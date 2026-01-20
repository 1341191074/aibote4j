const net = require('net');
const fs = require('fs');
const WindowsBot = require('WindowsBot');//引用WindowsBot模块
const os = require('os');
const path = require("path");
const child_process = require('child_process');

class Mutex {
    constructor () {
        this.queue = [];
        this.locked = false;
    }

    lock () {
        return new Promise((resolve, reject) => {
            if (this.locked) {
                this.queue.push([resolve, reject]);
            } else {
                this.locked = true;
                resolve();
            }
        });
    }

    release () {
        if (this.queue.length > 0) {
            const [resolve, reject] = this.queue.shift();
            resolve();
        } else {
            this.locked = false;
        }
    }
}

class AndroidBot{
    static server = [];
    static windowsBot = null;
    static androidIds = null;
    static hidMutex = new Mutex();
    /**
     * @param {function(AndroidBot)} androidMain 要注册的函数，必须含一个参数，用作接收AndroidBot对象
     * @param {number} port 监听端口, 默认16678
     */
    static registerMain(androidMain, port = 16678){
        let serverIndex = AndroidBot.server.length;
        AndroidBot.server[serverIndex] = new net.createServer();
        AndroidBot.server[serverIndex].listen(port);
        AndroidBot.server[serverIndex].on('connection', (clientSocket) => {
            androidMain(new AndroidBot(clientSocket));
        });
    }

    //构造函数
    socket;
    resolveHand;
    recvData;
    recvDataLen;
    isFirstData;
    waitTimeout;
    intervalTimeout;
    mutex;
    androidId;
    constructor(clientSocket) {
        this.androidId = null;
        this.socket = clientSocket;
        this.resolveHand = null;
        //this.recvData = "";
        this.recvData = Buffer.alloc(0);//字节数组处理
        this.recvDataLen = 0;//接收总长度
        this.isFirstData = true;//标记write首次触发data事件
        this.waitTimeout = 0;//隐式等待超时
        this.intervalTimeout = 1;//每次等待的时间
        this.mutex = new Mutex();
        this.socket.on('error', error=>{
            console.log('连接错误' + error);
            //this.#sockset.end();
        });
        this.socket.on( 'close', ()=>{
            console.log('AndroidBot连接已关闭');
        });

        //接收客户端数据,字节编码
        this.socket.on('data', data => {
            if(this.isFirstData){
                this.isFirstData = false;
                let strData = data.toString();
                let index = strData.indexOf("/");
                this.recvDataLen = parseInt(strData.substring(0, index), 10);
                //重新赋值
                this.recvData = data.slice(index + 1);
            }else{
                this.recvData = Buffer.concat([this.recvData, data]);
            }

            if (this.resolveHand && this.recvDataLen == this.recvData.length) {
                //清理现场
                this.isFirstData = true;
                this.resolveHand(this.recvData);
                this.resolveHand = null;
                this.mutex.release();//释放锁
            }
        });
    }

    setSendData = (...arrArgs) =>{
        // len/len/len\ndata
        let strData = "";
        let tempStr = "";
        arrArgs.forEach((args) =>{
            if(args == null)
                args = "";
            tempStr += args;
            strData += Buffer.byteLength(args.toString(), "utf8");//获取包含中文实际长度
            strData += '/';
        });
        strData += '\n';
        strData += tempStr;
        return strData;
    }

    setSendFile = (functionName, androidFilePath, fileData) =>{
        let strData = "";
        strData += Buffer.byteLength(functionName);
        strData += "/";
        strData += Buffer.byteLength(androidFilePath);
        strData += "/";
        strData += fileData.length;
        strData += "\n";
        strData += functionName;
        strData += androidFilePath;

        let byteData = Buffer.concat([Buffer.from(strData), fileData]);
        return byteData;
    }

    /**发送数据
     * @param {string} strData
     * @return {string}
     */
    sendData = (strData)=>{
        return new Promise(async (resolve) => {
            await this.mutex.lock();//加队列锁，防止数据并发
            this.resolveHand = resolve;
            this.socket.write(strData);
        })
    }

    /**睡眠等待
     * @param {number} millisecond  等待时间,单位毫秒
     * @return {Promise.<void>}
     */
    async sleep(millisecond){
        return new Promise(resolve => {setTimeout(() => {resolve()}, millisecond)});
    }

    /**设置隐式等待
     * @param {number} waitMs  等待时间,单位毫秒
     * @param {number} intervalMs 心跳间隔，单位毫秒。可选参数，默认5毫秒
     */
    async setImplicitTimeout(waitMs, intervalMs = 5){
        this.waitTimeout = waitMs;
        this.intervalTimeout = intervalMs;
    }

    /**设置安卓客户端接收超时，默认为永久等待
     * @param {number} recvTimeout 超时时间，单位毫秒
     * @return {Promise.<boolean>} 一般返回true
     */
    async setAndroidTimeout(recvTimeout){
        let strData = this.setSendData("setAndroidTimeout", recvTimeout);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**截图保存
     * @param {string} savePath 保存的位置
     * @param {{region:[left:number, top:number, right:number, bottom:number], threshold:[thresholdType:number, thresh:number, maxval:number]}} options 可选参数
     * region截图区域 [10, 20, 100, 200]，region默认全屏
     * threshold二值化图片, thresholdType算法类型：
     0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0
     1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva
     2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0
     3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变
     4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变
     5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值
     6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     thresh阈值，maxval最大值，threshold默认保存原图。thresh和maxval同为255时灰度处理
     * @return {Promise.<boolean>}
     */
    async saveScreenshot(savePath, options = {}){
        let left = 0, top = 0, right = 0, bottom = 0;
        let thresholdType = 0, thresh = 0, maxval = 0;
        if(options["region"] != undefined){
            left = options["region"][0];
            top = options["region"][1];
            right = options["region"][2];
            bottom = options["region"][3];
        }
        if(options["threshold"] != undefined){
            thresholdType = options["threshold"][0];
            if(thresholdType == 5 || thresholdType == 6){
                thresh = 127;
                maxval = 255;
            }else{
                thresh = options["threshold"][1];
                maxval = options["threshold"][2];
            }
        }

        let strData = this.setSendData("saveScreenshot", savePath, left, top, right, bottom, thresholdType, thresh, maxval);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**截图
     * @param {{region:[left:number, top:number, right:number, bottom:number], threshold:[thresholdType:number, thresh:number, maxval:number], scale:number}} options 可选参数
     * region截图区域 [10, 20, 100, 200]，region默认全屏
     * threshold二值化图片, thresholdType算法类型：
     0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0
     1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva
     2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0
     3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变
     4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变
     5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值
     6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     thresh阈值，maxval最大值，threshold默认保存原图。thresh和maxval同为255时灰度处理
     * scale浮点型 图片缩放率, 默认为 1.0 原大小。小于1.0缩小，大于1.0放大
     * @return {Promise.<ArrayBuffer>} 成功返回图像字节格式，失败返回"null"的字节格式
     */
    async takeScreenshot(options = {}){
        let left = 0, top = 0, right = 0, bottom = 0;
        let thresholdType = 0, thresh = 0, maxval = 0;
        let scale = 1.0;
        if(options["region"] != undefined){
            left = options["region"][0];
            top = options["region"][1];
            right = options["region"][2];
            bottom = options["region"][3];
        }
        if(options["threshold"] != undefined){
            thresholdType = options["threshold"][0];
            if(thresholdType == 5 || thresholdType == 6){
                thresh = 127;
                maxval = 255;
            }else{
                thresh = options["threshold"][1];
                maxval = options["threshold"][2];
            }
        }

        if(options["scale"] != undefined)
            scale = options["scale"];

        let strData = this.setSendData("takeScreenshot", left, top, right, bottom, thresholdType, thresh, maxval, scale);
        let byteRet = await this.sendData(strData);
        return byteRet;
    }

    /**获取指定坐标点的色值
     * @param {number} x 横坐标
     * @param {number} y 纵坐标
     * @return {Promise.<string>} 成功返回#开头的颜色值，失败返回null
     */
    async getColor(x, y){
        let strData = this.setSendData("getColor", x, y);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**找图
     * @param {string} imagePath 小图片路径（手机）,多张小图查找应当用"|"分开小图路径。工具截图保存目录：/storage/emulated/0/Android/data/com.aibot.client/files/
     * @param {{region:[left:number, top:number, right:number, bottom:number], sim:number, threshold:[thresholdType:number, thresh:number, maxval:number], multi:number}} options 可选参数
     * region 指定区域找图 [10, 20, 100, 200]，region默认全屏
     * sim浮点型 图片相似度 0.0-1.0，sim默认0.95
     * threshold二值化图片, thresholdType算法类型：
     0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0
     1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva
     2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0
     3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变
     4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变
     5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值
     6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     thresh阈值，maxval最大值，threshold默认保存原图。thresh和maxval同为255时灰度处理
     * multi 找图数量，默认为1 找单个图片坐标
     * @return {Promise.<[{x:number, y:number}]>} 成功返回 单坐标点[{x:number, y:number}]，多坐标点[{x1:number, y1:number}, {x2:number, y2:number}...] 失败返回null
     */
    async findImage(imagePath, options = {}){
        //如果是文件名，这里添加默认路径
        const parsed = path.parse(imagePath);
        if (!parsed.dir)
            imagePath = "/storage/emulated/0/Android/data/com.aibot.client/files/" + imagePath;

        let left = 0, top = 0, right = 0, bottom = 0;
        let sim = 0.95;
        let thresholdType = 0, thresh = 0, maxval = 0;
        let multi = 1;
        if(options["region"] != undefined){
            left = options["region"][0];
            top = options["region"][1];
            right = options["region"][2];
            bottom = options["region"][3];
        }
        if(options["sim"] != undefined)
            sim = options["sim"];
        if(options["threshold"] != undefined){
            thresholdType = options["threshold"][0];
            if(thresholdType == 5 || thresholdType == 6){
                thresh = 127;
                maxval = 255;
            }else{
                thresh = options["threshold"][1];
                maxval = options["threshold"][2];
            }
        }
        if(options["multi"] != undefined)
            multi = options["multi"];

        let strData = this.setSendData("findImage", imagePath, left, top, right, bottom, sim, thresholdType, thresh, maxval, multi);
        let strRet, byteRet;
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "-1.0|-1.0")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "-1.0|-1.0")
            return null;

        let arrPoints = strRet.split("/");
        let pointCount = arrPoints.length;
        let arrRet = [];
        let arrPoint;
        for(let i = 0; i < pointCount; i++){
            arrPoint = arrPoints[i].split("|");
            arrRet[i] = {x: parseInt(arrPoint[0]), y: parseInt(arrPoint[1])};
        }
        return arrRet;
    }

    /**找动态图
     * @param {number} frameRate 前后两张图相隔的时间，单位毫秒
     * @param {{region:[left:number, top:number, right:number, bottom:number]}} options 可选参数
     * region 指定区域找图 [10, 20, 100, 200]，region默认全屏
     * @return {Promise.<[{x:number, y:number}]>} 成功返回 单坐标点[{x:number, y:number}]，多坐标点[{x1:number, y1:number}, {x2:number, y2:number}...] 失败返回null
     */
    async findAnimation(frameRate, options = {}){
        let left = 0, top = 0, right = 0, bottom = 0;
        if(options["region"] != undefined){
            left = options["region"][0];
            top = options["region"][1];
            right = options["region"][2];
            bottom = options["region"][3];
        }

        let strData = this.setSendData("findAnimation", frameRate, left, top, right, bottom);
        let strRet, byteRet;
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        do{
            byteRet = await this.sendData(strData);
            strRet=  byteRet.toString();
            if(strRet == "-1.0|-1.0")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "-1.0|-1.0")
            return null;

        let arrPoints = strRet.split("/");
        let pointCount = arrPoints.length;
        let arrRet = [];
        let arrPoint;
        for(let i = 0; i < pointCount; i++){
            arrPoint = arrPoints[i].split("|");
            arrRet[i] = {x: parseInt(arrPoint[0]), y: parseInt(arrPoint[1])};
        }
        return arrRet;
    }

    /**查找指定色值的坐标点
     * @param {string} strMainColor #开头的色值
     * @param {{subColors:[[offsetX:number, offsetY:number, strSubColor:string], ...], region:[left:number, top:number, right:number, bottom:number], sim:number}} options 可选参数
     * subColors 相对于strMainColor 的子色值，[[offsetX, offsetY, "#FFFFFF"], ...]，subColors默认为null
     * region 指定区域找色 [10, 20, 100, 200]，region默认全屏
     * sim相似度0.0-1.0，sim默认为0.98
     * @return {Promise.<{x:number, y:number}>} 成功返回{x:number, y:number} 失败返回null
     */
    async findColor(strMainColor, options = {}){
        let strSubColors = "null";
        let left = 0, top = 0, right = 0, bottom = 0;
        let sim = 0.98;
        if(options["subColors"] != undefined){
            strSubColors = "";
            let arrLen = options["subColors"].length;
            for(let i = 0; i < arrLen; i++){
                strSubColors += options["subColors"][i][0] + "/";
                strSubColors += options["subColors"][i][1] + "/";
                strSubColors += options["subColors"][i][2];
                if(i < arrLen - 1)
                    strSubColors += "\n";
            }
        }
        if(options["region"] != undefined){
            left = options["region"][0];
            top = options["region"][1];
            right = options["region"][2];
            bottom = options["region"][3];
        }
        if(options["sim"] != undefined)
            sim = options["sim"];

        let strData = this.setSendData("findColor", strMainColor, strSubColors, left, top, right, bottom, sim);
        let strRet, byteRet;
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "-1.0|-1.0")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "-1.0|-1.0")
            return null;
        let arrRet = strRet.split("|");
        return {x: parseInt(arrRet[0]), y: parseInt(arrRet[1])};
    }

    /**比较指定坐标点的颜色值
     * @param {number} mainX 主颜色所在的X坐标
     * @param {number} mainY 主颜色所在的Y坐标
     * @param {string} strMainColor #开头的色值
     * @param {{subColors:[[offsetX:number, offsetY:number, strSubColor:string], ...], region:[left:number, top:number, right:number, bottom:number], sim:number}} options 可选参数
     * subColors 相对于strMainColor 的子色值，[[offsetX, offsetY, "#FFFFFF"], ...]，subColors默认为null
     * region 指定区域找色 [10, 20, 100, 200]，region默认全屏
     * sim相似度0.0-1.0，sim默认为0.98
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    async compareColor(mainX, mainY, strMainColor, options = {}){
        let strSubColors = "null";
        let left = 0, top = 0, right = 0, bottom = 0;
        let sim = 0.98;
        if(options["subColors"] != undefined){
            strSubColors = "";
            let arrLen = options["subColors"].length;
            for(let i = 0; i < arrLen; i++){
                strSubColors += options["subColors"][i][0] + "/";
                strSubColors += options["subColors"][i][1] + "/";
                strSubColors += options["subColors"][i][2];
                if(i < arrLen - 1)
                    strSubColors += "\n";
            }
        }
        if(options["region"] != undefined){
            left = options["region"][0];
            top = options["region"][1];
            right = options["region"][2];
            bottom = options["region"][3];
        }
        if(options["sim"] != undefined)
            sim = options["sim"];

        let strData = this.setSendData("compareColor", mainX, mainY, strMainColor, strSubColors, left, top, right, bottom, sim);
        let strRet, byteRet;
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString()
            if(strRet == "false")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**手指按下
     * @param {number} x 横坐标
     * @param {number} y 纵坐标
     * @param {number} duration 按下时长，单位毫秒
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async press(x, y, duration){
        let strData = this.setSendData("press", x, y, duration);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**手指移动
     * @param {number} x 横坐标
     * @param {number} y 纵坐标
     * @param {number} duration 移动时长，单位毫秒
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async move(x, y, duration){
        let strData = this.setSendData("move", x, y, duration);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**手指释放
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async release(){
        let strData = this.setSendData("release");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**点击坐标
     * @param {number} x 横坐标
     * @param {number} y 纵坐标
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async click(x, y){
        let strData = this.setSendData("click", x, y);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**双击坐标
     * @param {number} x 横坐标
     * @param {number} y 纵坐标
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async doubleClick(x, y){
        let strData = this.setSendData("doubleClick", x, y);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**长按坐标
     * @param {number} x 横坐标
     * @param {number} y 纵坐标
     * @param {number} duration 长按时长，单位毫秒
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async longClick(x, y, duration){
        let strData = this.setSendData("longClick", x, y, duration);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**滑动坐标
     * @param {number} startX 起始横坐标
     * @param {number} startY 起始纵坐标
     * @param {number} endX 结束横坐标
     * @param {number} endY 结束纵坐标
     * @param {number} duration 滑动时长，单位毫秒
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async swipe(startX, startY, endX, endY, duration){
        let strData = this.setSendData("swipe", startX, startY, endX, endY, duration);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**执行手势
     * @param {[[x:number, y:number], [x1:number, y1:number]...]} gesturePath 手势路径
     * @param {number} duration 手势时长，单位毫秒
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async dispatchGesture(gesturePath, duration){
        let strGesturePath = "";
        let arrLen = gesturePath.length;
        for(let i = 0; i < arrLen; i++){
            strGesturePath += gesturePath[i][0] + "/";
            strGesturePath += gesturePath[i][1] + "/";
            if(i < arrLen - 1)
                strGesturePath += "\n";
        }

        let strData = this.setSendData("dispatchGesture", strGesturePath, duration);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**执行多个手势
     * @param {[[duration:number, [x:number, y:number], [x1:number, y1:number]...],[duration:number, [x:number, y:number], [x1:number, y1:number]...],...]} gesturesPath  多点手势路径
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async dispatchGestures(gesturesPath){
        let strGesturesPath = "";
        let arrLen1 = gesturesPath.length;
        for(let i = 0; i < arrLen1; i++){
            let arrLen2 = gesturesPath[i].length;
            strGesturesPath += gesturesPath[i][0] + "/";
            for(let j = 1; j < arrLen2; j++){
                strGesturesPath += gesturesPath[i][j][0] + "/";
                strGesturesPath += gesturesPath[i][j][1] + "/";
                if(j < arrLen2 - 1)
                    strGesturesPath += "\n";
            }
            if(i < arrLen1 - 1)
                strGesturesPath += "\r\n";
        }

        let strData = this.setSendData("dispatchGestures", strGesturesPath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**发送文本
     * @param {string} text 发送的文本，需要打开aibote输入法
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async sendKeys(text){
        let strData = this.setSendData("sendKeys", text);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**发送按键
     * @param {number} keyCode 发送的虚拟按键，需要打开aibote输入法。例如：最近应用列表：187  回车：66
     * 按键对照表 https://blog.csdn.net/yaoyaozaiye/article/details/122826340
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async sendVk(keyCode){
        let strData = this.setSendData("sendVk", keyCode);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**返回
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async back(){
        let strData = this.setSendData("back");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**home
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async home(){
        let strData = this.setSendData("home");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**显示最近任务
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async recents(){
        let strData = this.setSendData("recents");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**打开 开/关机 对话框，基于无障碍权限
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async powerDialog(){
        let strData = this.setSendData("powerDialog");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**ocr
     * @param {string} ocrServerIp ocr服务端IP，端口固定为9527。当参数值为 "127.0.0.1"时，则使用手机内置的ocr识别
     * @param {left:number} left 左上角x点
     * @param {top:number} top 左上角y点
     * @param {right:number} right 右下角 x点
     * @param {bottom:number} bottom 右下角 y点
     * @param {thresholdType:number} thresholdType 二值化算法类型
     * @param {thresh:number} thresh 阈值
     * @param {maxval:number} maxval 最大值
     * @param {scale:number} scale 图片缩放率, 默认为 1.0 原大小。大于1.0放大，小于1.0缩小，不能为负数。
     * @return {Promise.<[]>} 失败返回null，成功返回数组形式的识别结果
     */
    ocr = async (ocrServerIp, left, top, right, bottom, thresholdType, thresh, maxval, scale) =>{
        let strData = this.setSendData("ocr", ocrServerIp, left, top, right, bottom, thresholdType, thresh, maxval, scale);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == '' || strRet == "null" || strRet == "[]")
            return null;
        else
            return JSON.parse(strRet);
    }

    /**获取屏幕文字
     * @param {string} ocrServerIp ocr服务端IP，端口固定为9527。当参数值为 "127.0.0.1"时，则使用手机内置的ocr识别
     * @param {{region:[left:number, top:number, right:number, bottom:number], threshold:[thresholdType:number, thresh:number, maxval:number], scale:number}} options 可选参数
     * region 指定区域 [10, 20, 100, 200]，region默认全屏
     * threshold二值化图片, thresholdType算法类型：
     0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0
     1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva
     2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0
     3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变
     4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变
     5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值
     6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     thresh阈值，maxval最大值，threshold默认保存原图。thresh和maxval同为255时灰度处理
     * scale浮点型 图片缩放率, 默认为 1.0 原大小。大于1.0放大，小于1.0缩小，不能为负数。
     * @return {Promise.<[string]>} 失败返回null，成功以数组字符串的形式返回手机屏幕上的文字
     */
    async getWords(ocrServerIp, options = {}){
        let left = 0, top = 0, right = 0, bottom = 0;
        let thresholdType = 0, thresh = 0, maxval = 0;
        let scale = 1.0;
        if(options["region"] != undefined){
            left = options["region"][0];
            top = options["region"][1];
            right = options["region"][2];
            bottom = options["region"][3];
        }
        if(options["threshold"] != undefined){
            thresholdType = options["threshold"][0];
            if(thresholdType == 5 || thresholdType == 6){
                thresh = 127;
                maxval = 255;
            }else{
                thresh = options["threshold"][1];
                maxval = options["threshold"][2];
            }
        }
        if(options["scale"] != undefined)
            scale = options["scale"];

        let wordsResult = await this.ocr(ocrServerIp, left, top, right, bottom, thresholdType, thresh, maxval, scale);
        if(wordsResult == null)
            return null;

        const words = wordsResult.map(item => item.text);
        return words;
    }

    /**查找文字
     * @param {string} ocrServerIp ocr服务端IP，端口固定为9527。当参数值为 "127.0.0.1"时，则使用手机内置的ocr识别
     * @param {string} words 要查找的文字
     * @param {{region:[left:number, top:number, right:number, bottom:number], threshold:[thresholdType:number, thresh:number, maxval:number], scale:number}} options 可选参数
     * region 指定区域 [10, 20, 100, 200]，region默认全屏
     * threshold二值化图片, thresholdType算法类型：
     0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0
     1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva
     2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0
     3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变
     4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变
     5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值
     6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     thresh阈值，maxval最大值，threshold默认保存原图。thresh和maxval同为255时灰度处理
     * scale浮点型 图片缩放率, 默认为 1.0 原大小。大于1.0放大，小于1.0缩小，不能为负数。
     * @return {Promise.<[{x:number, y:number}]>} 失败返回null，成功返回数组[{x:number, y:number}, ...]，文字所在的坐标点
     */
    async findWords(ocrServerIp, words, options = {}){
        let left = 0, top = 0, right = 0, bottom = 0;
        let thresholdType = 0, thresh = 0, maxval = 0;
        let scale = 1.0;
        if(options["region"] != undefined){
            left = options["region"][0];
            top = options["region"][1];
            right = options["region"][2];
            bottom = options["region"][3];
        }
        if(options["threshold"] != undefined){
            thresholdType = options["threshold"][0];
            if(thresholdType == 5 || thresholdType == 6){
                thresh = 127;
                maxval = 255;
            }else{
                thresh = options["threshold"][1];
                maxval = options["threshold"][2];
            }
        }
        if(options["scale"] != undefined)
            scale = options["scale"];

        let wordsResult = await this.ocr(ocrServerIp, left, top, right, bottom, thresholdType, thresh, maxval, scale);
        if(wordsResult == null)
            return null;

        const texts = wordsResult.map(item => item.text);
        const boxs = wordsResult.map(item => item.box);
        const idxVals = texts.map((v, i) => (v === words ? i : -1)).filter(i => i !== -1);

        let points = [];
        for(let i = 0; i < idxVals.length; i++){
            let index = idxVals[i];
            let localLeft = boxs[index][0];
            let localTop = boxs[index][1];
            let localRight = boxs[index][2];
            let localBottom = boxs[index][3];

            let width = localRight - localLeft;
            let height = localBottom - localTop;

            let wordWidth = width / texts[index].length;
            let offsetX = wordWidth * (words.length / 2);
            let offsetY = height / 2;
            let x = parseInt((localLeft + offsetX) / scale + left);
            let y = parseInt((localTop + offsetY) / scale + top);
            points[i] = {"x":x, "y":y};
        }

        if(points.length == 0)
            return null;
        else
            return points;
    }

    /**yolo
     * @param {string} yoloServerIp yolo服务端IP，端口固定为9528。
     * @param {{region:[left:number, top:number, right:number, bottom:number], scale:number}} options 可选参数
     * region 指定区域 [10, 20, 100, 200]，region默认全屏。区域设置应当和训练时区域一致
     * scale浮点型 图片缩放率, 默认为 1.0 原大小。大于1.0放大，小于1.0缩小，不能为负数。缩放设置应当和训练时缩放一致
     * @return {Promise.<[JSON]>} 失败返回null，成功返回数组json形式的识别结果。
     */
    async yolo(yoloServerIp, options = {}){
        let left = 0, top = 0, right = 0, bottom = 0;
        let scale = 1.0;
        if(options["region"] != undefined){
            left = options["region"][0];
            top = options["region"][1];
            right = options["region"][2];
            bottom = options["region"][3];
        }
        if(options["scale"] != undefined)
            scale = options["scale"];


        let strData = this.setSendData("yolo", yoloServerIp, left, top, right, bottom, scale);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == '' || strRet == "null" || strRet == "[]")
            return null;
        else{//处理缩放
            let retJson = JSON.parse(strRet);
            for(let i = 0; i < retJson.length; i++){
                retJson[i]['box'][0] /= scale;
                retJson[i]['box'][1] /= scale;
                retJson[i]['box'][2] /= scale;
                retJson[i]['box'][3] /= scale;
            }
            return retJson;
        }
    }

    /**URL请求
     * @param {string} url 请求的地址 http://www.ai-bot.net
     * @param {string} requestType 请求类型，GET或者POST
     * @param {string} headers 可选参数，请求头
     * @param {string} postData 可选参数，用作POST 提交的数据
     * @return {Promise.<string>} 返回请求数据内容
     */
    async urlRequest(url, requestType, headers = "null", postData = "null"){
        let strData = this.setSendData("urlRequest", url, requestType, headers, postData);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet
    }

    /**Toast消息提示
     * @param {string} text 提示的文本
     * @param {number} duration 显示时长，最大时长3500毫秒
     * @return {Promise.<boolean>} 返回true
     */
    async showToast(text, duration){
        let strData = this.setSendData("showToast", text, duration);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**启动App
     * @param {string} name 包名或者app名称
     * @return {Promise.<boolean>} 成功返回true 失败返回false。非Aibote界面时候调用，需要开启悬浮窗
     */
    async startApp(name){
        let strData = this.setSendData("startApp", name);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**判断app是否正在运行(包含前后台)
     * @param {string} name 包名或者app名称
     * @return {Promise.<boolean>} 正在运行返回true，否则返回false
     */
    async appIsRunnig(name){
        let strData = this.setSendData("appIsRunnig", name);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取已安装app的包名(不包含系统APP)
     * @return {Promise.<string[] | null>} 成功返回已安装app包名数组，失败返回null
     */
    async getInstalledPackages(){
        let strData = this.setSendData("getInstalledPackages");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet.split("|");
    }

    /**屏幕大小
     * @return {Promise.<{width:number, height:number}>} 成功返回{width:number, height:number}
     */
    async getWindowSize(){
        let strData = this.setSendData("getWindowSize");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        let arrRet = strRet.split("|");

        return {width: parseInt(arrRet[0]), height: parseInt(arrRet[1])};
    }

    /**图片大小
     * @param {string} imagePath 图片路径
     * @return {Promise.<{width:number, height:number}>} 成功返回{width:number, height:number}
     */
    async getImageSize(imagePath){
        let strData = this.setSendData("getImageSize", imagePath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        let arrRet = strRet.split("|");

        return {width: parseInt(arrRet[0]), height: parseInt(arrRet[1])};
    }

    /**获取安卓ID
     * @return {Promise.<string>} 成功返回安卓手机ID
     */
    async getAndroidId(){
        let strData = this.setSendData("getAndroidId");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**获取投屏组号
     * @return {Promise.<string>} 成功返回投屏组号
     */
    async getGroup(){
        let strData = this.setSendData("getGroup");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**获取投屏编号
     * @return {Promise.<string>} 成功返回投屏编号
     */
    async getIdentifier(){
        let strData = this.setSendData("getIdentifier");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**获取投屏标题
     * @return {Promise.<string>} 成功返回投屏标题
     */
    async getTitle(){
        let strData = this.setSendData("getTitle");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**识别验证码
     * @param {string} filePath 图片文件路径
     * @param {string} username 用户名
     * @param {string} password 密码
     * @param {string} softId 软件ID
     * @param {string} codeType 图片类型 参考https://www.chaojiying.com/price.html
     * @param {string} lenMin 最小位数 默认0为不启用,图片类型为可变位长时可启用这个参数
     * @return {Promise.<{err_no:number, err_str:string, pic_id:string, pic_str:string, md5:string}>} 返回JSON
     * err_no,(数值) 返回代码  为0 表示正常，错误代码 参考https://www.chaojiying.com/api-23.html
     * err_str,(字符串) 中文描述的返回信息
     * pic_id,(字符串) 图片标识号，或图片id号
     * pic_str,(字符串) 识别出的结果
     * md5,(字符串) md5校验值,用来校验此条数据返回是否真实有效
     */
    async getCaptcha(filePath, username, password, softId, codeType, lenMin = 0){
        let strData = this.setSendData("getCaptcha", filePath, username, password, softId, codeType, lenMin);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return JSON.parse(strRet);
    }

    /**识别报错返分
     * @param {string} username 用户名
     * @param {string} password 密码
     * @param {string} softId 软件ID
     * @param {string} picId 图片ID 对应 getCaptcha返回值的pic_id 字段
     * @return {Promise.<{err_no:number, err_str:string}>} 返回JSON
     * err_no,(数值) 返回代码
     * err_str,(字符串) 中文描述的返回信息
     */
    async errorCaptcha(username, password, softId, picId){
        let strData = this.setSendData("errorCaptcha", username, password, softId, picId);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return JSON.parse(strRet);
    }

    /**查询验证码剩余题分
     * @param {string} username 用户名
     * @param {string} password 密码
     * @return {Promise.<{err_no:number, err_str:string, tifen:string, tifen_lock:string}>} 返回JSON
     * err_no,(数值) 返回代码
     * err_str,(字符串) 中文描述的返回信息
     * tifen,(数值) 题分
     * tifen_lock,(数值) 锁定题分
     */
    async scoreCaptcha(username, password){
        let strData = this.setSendData("scoreCaptcha", username, password);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return JSON.parse(strRet);
    }

    /**获取可见区域内的所有元素信息
     * @param {string|null} ocrServerIp ocr服务端IP，端口固定为9527，默认为 null。若提供此参数，则返回结果增加 words 字段，包含界面中所有的文字区域
     * @return {Promise.<{elements:[{}]}|{elements:[{}]}, words:[{}]|null>} 成功返回json格式的元素信息，失败返回null
     */
    async getElements(ocrServerIp = null){
        let strData = this.setSendData("getElements");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        try{
            let elementsJson = JSON.parse(strRet);
            if(ocrServerIp != null && ocrServerIp != ''){
                let words = await this.ocr(ocrServerIp, 0, 0, 0, 0, 0, 0, 0, 1.0);
                elementsJson["words"] = words;
            }
            return elementsJson;
        }catch(e){
            return null;
        }
    }

    /**获取元素位置
     * @param {string} xpath 元素路径
     * @return {Promise.<{left:number, top:number, right:number, bottom:number}>} 成功返回元素位置，失败返回null
     */
    async getElementRect(xpath){
        let strData = this.setSendData("getElementRect", xpath);
        let strRet, byteRet;
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "-1|-1|-1|-1")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "-1|-1|-1|-1")
            return null;
        let arrRet = strRet.split("|");
        return {left: parseInt(arrRet[0]), top: parseInt(arrRet[1]), right: parseInt(arrRet[2]), bottom: parseInt(arrRet[3])};
    }

    /**获取元素描述
     * @param {string} xpath 元素路径
     * @return {Promise.<string>} 成功返回元素内容，失败返回null
     */
    async getElementDescription(xpath){
        let strData = this.setSendData("getElementDescription", xpath);
        let strRet, byteRet;
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "null")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**获取元素文本
     * @param {string} xpath 元素路径
     * @return {Promise.<string>} 成功返回元素内容，失败返回null
     */
    async getElementText(xpath){
        let strData = this.setSendData("getElementText", xpath);
        let strRet, byteRet;
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "null")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**判断元素是否可见
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 可见 ture，不可见 false
     */
    async elementIsVisible(xpath){
        let windowRect = await this.getWindowSize();
        let elementRect = await this.getElementRect(xpath);
        if(elementRect == null)
            return false;

        let elementWidth = elementRect.right - elementRect.left;
        let elementHeight = elementRect.bottom - elementRect.top;
        if(elementRect.top < 0 || elementRect.left < 0 || elementWidth > windowRect.width || elementHeight > windowRect.height)
            return false;
        else
            return true;
    }

    /**设置元素文本
     * @param {string} xpath 元素路径
     * @param {string} text 设置的文本
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async setElementText(xpath, text){
        let strData = this.setSendData("setElementText", xpath, text);
        let strRet, byteRet;
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "false")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**点击元素
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async clickElement(xpath){
        let strData = this.setSendData("clickElement", xpath);
        let strRet, byteRet;
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "false")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**滚动元素
     * @param {string} xpath 元素路径
     * @param {number} direction 0 向前滑动， 1 向后滑动
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async scrollElement(xpath, direction){
        let strData = this.setSendData("scrollElement", xpath, direction);
        let strRet, byteRet;
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "false")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**判断元素是否存在
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async existsElement(xpath){
        let strData = this.setSendData("existsElement", xpath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();

        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**判断元素是否选中
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async isSelectedElement(xpath){
        let strData = this.setSendData("isSelectedElement", xpath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();

        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**判断元素是否启用
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async isEnabledElement(xpath){
        let strData = this.setSendData("isEnabledElement", xpath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();

        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**上传文件
     * @param {string} windowsFilePath 电脑文件路径，注意电脑路径 "\\"转义问题
     * @param {string} androidFilePath 安卓文件保存路径, 安卓外部存储根目录 /storage/emulated/0/
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async pushFile(windowsFilePath, androidFilePath){
        let fileData = await fs.readFileSync(windowsFilePath);
        let byteData = await this.setSendFile("pushFile", androidFilePath, fileData);
        let byteRet = await this.sendData(byteData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**拉取文件
     * @param {string} androidFilePath 安卓文件路径，安卓外部存储根目录 /storage/emulated/0/
     * @param {string} windowsFilePath 电脑文件保存路径，注意电脑路径 "\\"转义问题
     * @return {Promise.<void>}
     */
    async pullFile(androidFilePath, windowsFilePath){
        let strData = this.setSendData("pullFile", androidFilePath);
        let byteRet = await this.sendData(strData);
        await fs.writeFileSync(windowsFilePath, byteRet);
    }

    /**GET 下载url文件
     * @param {string} url 文件请求地址
     * @param {string} savePath 安卓文件路径，安卓外部存储根目录 /storage/emulated/0/
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async downloadFile(url, savePath){
        let strData = this.setSendData("downloadFile", url, savePath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**写入安卓文件
     * @param {string} androidFilePath 安卓文件路径，安卓外部存储根目录 /storage/emulated/0/
     * @param {string} text 写入的内容
     * @param {boolean} isAppend 可选参数，是否追加，默认覆盖文件内容
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async writeAndroidFile(androidFilePath, text, isAppend = false){
        let strData = this.setSendData("writeAndroidFile", androidFilePath, text, isAppend);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**读取安卓文件
     * @param {string} androidFilePath 安卓文件路径，安卓外部存储根目录 /storage/emulated/0/
     * @return {Promise.<string>} 成功返回文件内容，失败返回 null
     */
    async readAndroidFile(androidFilePath){
        let strData = this.setSendData("readAndroidFile", androidFilePath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**删除安卓文件
     * @param {string} androidFilePath 安卓文件路径，安卓外部存储根目录 /storage/emulated/0/
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async deleteAndroidFile(androidFilePath){
        let strData = this.setSendData("deleteAndroidFile", androidFilePath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**判断文件是否存在
     * @param {string} androidFilePath 安卓文件路径，安卓外部存储根目录 /storage/emulated/0/
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async existsAndroidFile(androidFilePath){
        let strData = this.setSendData("existsAndroidFile", androidFilePath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取文件夹内的所有文件(不包含深层子目录)
     * @param {string} androidDirectory 安卓目录，安卓外部存储根目录 /storage/emulated/0/
     * @return {Promise.<string[] | null>} 成功返回所有子文件名称，失败返回null
     */
    async getAndroidSubFiles(androidDirectory){
        let strData = this.setSendData("getAndroidSubFiles", androidDirectory);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet.split("|");
    }

    /**创建安卓文件夹
     * @param {string} androidDirectory 安卓目录
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async makeAndroidDir(androidDirectory){
        let strData = this.setSendData("makeAndroidDir", androidDirectory);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**Intent 跳转
     * @param {string} action 动作，例如 "android.intent.action.VIEW"
     * @param {{uri:string, packageName:string, className:string, type:string}} options 可选参数
     * uri 跳转链接，可选参数 例如：打开支付宝扫一扫界面，"alipayqr://platformapi/startapp?saId=10000007"
     * ackageName 包名，可选参数 "com.xxx.xxxxx"
     * className 类名，可选参数
     * type 类型，可选参数
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async startActivity(action, options = {}){
        let uri = "", packageName = "", className = "", type = "";
        if(options["uri"] != undefined)
            uri = options["uri"];
        if(options["packageName"] != undefined)
            packageName = options["packageName"];
        if(options["className"] != undefined)
            className = options["className"];
        if(options["type"] != undefined)
            type = options["type"];

        let strData = this.setSendData("startActivity", action, uri, packageName, className, type);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**拨打电话
     * @param {string} phoneNumber 拨打的电话号码
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async callPhone(phoneNumber){
        let strData = this.setSendData("callPhone", phoneNumber);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**发送短信
     * @param {string} phoneNumber 发送的电话号码
     * @param {string} message 短信内容
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async sendMsg(phoneNumber, message){
        let strData = this.setSendData("sendMsg", phoneNumber, message);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取当前活动窗口(Activity)
     * @return {Promise.<string>} 成功返回当前activity
     */
    async getActivity(){
        let strData = this.setSendData("getActivity");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**获取当前活动包名(Package)
     * @return {Promise.<string>} 成功返回当前包名
     */
    async getPackage(){
        let strData = this.setSendData("getPackage");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**设置剪切板文本
     * @param {string} text 设置的文本
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async setClipboardText(text){
        let strData = this.setSendData("setClipboardText", text);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取剪切板文本
     * @return {Promise.<string>} 需要打开aibote输入法。成功返回剪切板文本，失败返回null
     */
    async getClipboardText(){
        let strData = this.setSendData("getClipboardText");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**创建TextView控件
     * @param {number} id 控件ID，不可与其他控件重复
     * @param {string} text 控件文本
     * @param {number} x 控件在屏幕上x坐标
     * @param {number} y 控件在屏幕上y坐标
     * @param {number} width 控件宽度
     * @param {number} height 控件高度
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async createTextView(id, text, x, y, width, height){
        let strData = this.setSendData("createTextView", id, text, x, y, width, height);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**创建EditText控件
     * @param {number} id 控件ID，不可与其他控件重复
     * @param {string} hintText 提示文本
     * @param {number} x 控件在屏幕上x坐标
     * @param {number} y 控件在屏幕上y坐标
     * @param {number} width 控件宽度
     * @param {number} height 控件高度
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async createEditText(id, hintText, x, y, width, height){
        let strData = this.setSendData("createEditText", id, hintText, x, y, width, height);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**创建CheckBox控件
     * @param {number} id 控件ID，不可与其他控件重复
     * @param {string} text 控件文本
     * @param {number} x 控件在屏幕上x坐标
     * @param {number} y 控件在屏幕上y坐标
     * @param {number} width 控件宽度
     * @param {number} height 控件高度
     * @param {boolean} isSelect 是否勾选
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async createCheckBox(id, text, x, y, width, height, isSelect){
        let strData = this.setSendData("createCheckBox", id, text, x, y, width, height, isSelect);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**创建ListText控件
     * @param {number} id 控件ID，不可与其他控件重复
     * @param {string} hintText 提示文本
     * @param {number} x 控件在屏幕上x坐标
     * @param {number} y 控件在屏幕上y坐标
     * @param {number} width 控件宽度
     * @param {number} height 控件高度
     * @param {string[]} listText 列表文本
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async createListText(id, hintText, x, y, width, height, listText){
        let strData = this.setSendData("createListText", id, hintText, x, y, width, height, listText);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**创建WebView控件
     * @param {number} id 控件ID，不可与其他控件重复
     * @param {string} url 加载的链接
     * @param {number} x 控件在屏幕上x坐标，值为-1时自动填充宽高
     * @param {number} y 控件在屏幕上y坐标，值为-1时自动填充宽高
     * @param {number} width 控件宽度，值为-1时自动填充宽高
     * @param {number} height 控件高度，值为-1时自动填充宽高
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async createWebView(id, url, x, y, width, height){
        let strData = this.setSendData("createWebView", id, url, x, y, width, height);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**创建SwitchButton控件
     * @param {number} id 控件ID，不可与其他控件重复
     * @param {string} text 控件文本
     * @param {number} x 控件在屏幕上x坐标
     * @param {number} y 控件在屏幕上y坐标
     * @param {number} width 控件宽度
     * @param {number} height 控件高度
     * @param {boolean} isChecked 是否打 开/关
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async createSwitchButton(id, text, x, y, width, height, isChecked){
        let strData = this.setSendData("createSwitchButton", id, text, x, y, width, height, isChecked);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**清除脚本控件
     * @return {Promise.<boolean>} 成功返回true，失败返回 false
     */
    async clearScriptControl(){
        let strData = this.setSendData("clearScriptControl");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取脚本配置参数
     * @return {Promise.<JSON>} 成功返回{"id":"text", "id":"isSelect"} 此类对象，失败返回null。函数仅返回TextEdit和CheckBox控件值，需要用户点击安卓端 "提交参数" 按钮
     */
    async getScriptParam(){
        let strData = this.setSendData("getScriptParam");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        strRet = strRet.replace(/\r/g, "\\r");
        strRet = strRet.replace(/\n/g, "\\n");
        if(strRet == "null")
            return null;
        else
            return JSON.parse(strRet);
    }

    //启动windowsDriver.exe
    startWindowsBot = () =>{
        let driverName = 'WindowsDriver.exe';
        //win 7
        if(parseFloat(os.release()) < 10)
            driverName = "WindowsDriver_win7.exe";

        //获取驱动程序路径
        let driverPath = "../" + driverName;//path.resolve(__dirname, "../../../" + driverName);
        let isExist = fs.existsSync(driverPath);
        if(!isExist)
            driverPath = driverName;

        let ip = "127.0.0.1";
        let port = 56668;//固定端口
        child_process.execFile(driverPath, [ip, port]);
        console.log("正在启动WindowsDriver...");

        let serverIndex = WindowsBot.server.length;
        WindowsBot.server[serverIndex] = new net.createServer();
        WindowsBot.server[serverIndex].listen(port);
        WindowsBot.server[serverIndex].on('connection', (clientSocket) => {
            AndroidBot.windowsBot = new WindowsBot(clientSocket);
        });
    }

    //初始化android Accessory，获取手机hid相关的数据。 
    initAccessory = async () =>{
        let strData = this.setSendData("initAccessory");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**初始化Hid
     * @return {Promise.<boolean>} 成功返回true，失败返回 false。
     * hid实际上是由windowsBot 通过数据线直接发送命令给安卓系统并执行，并不是由aibote.apk执行的命令。
     * 我们应当将所有设备准备就绪再调用此函数初始化。
     * Windows initHid 和 android initAccessory函数 初始化目的是两者的数据交换，并告知windowsBot发送命令给哪台安卓设备
     */
    async initHid(){
        await AndroidBot.hidMutex.lock();//
        //启动windowsDriver,一次就行
        if(AndroidBot.windowsBot == null){
            await this.startWindowsBot();
            let tries = 5;//最大尝试5次
            while(!AndroidBot.windowsBot && tries){
                tries--;
                await this.sleep(1000);
            }

            if(AndroidBot.windowsBot == null)
                return false;

            //初始化windowsBot的hid相关函数
            //注意，这里调用的是 windowsBot的 "initHid"
            //windowsBot.initHid 和 initHid 在底层会交换hid相关数据
            if(!await AndroidBot.windowsBot.initHid())
                return false;
        }
        await AndroidBot.hidMutex.release();//

        //初始化android Accessory，获取手机hid相关的数据。 先调用 AndroidBot.windowsBot.initHid() 后再调用initAccessory() 顺序不能变
        if(!await this.initAccessory())
            return false;

        await AndroidBot.hidMutex.lock();//
        //先调用 windowsBot.initHid，再调用androidBot.initHid。
        //初始化完毕再通过windowsBot.getHidData获取交换后的hid相关的数据
        if(AndroidBot.androidIds == null)//不能重复调用
            AndroidBot.androidIds = await AndroidBot.windowsBot.getHidData();
        await AndroidBot.hidMutex.release();//

        let isSucceed = false;
        if(AndroidBot.androidIds == null)
            return isSucceed;

        //获取AndroidId 用作hid相关函数区分手机设备
        this.androidId = await this.getAndroidId();
        //遍历判断当前安卓设备hid激活是否成功
        for(let i = 0; i < AndroidBot.androidIds.length; i++){
            if(AndroidBot.androidIds[i] == this.androidId){
                isSucceed = true;
                break;
            }
        }

        return isSucceed;
    }

    /**按下
     * @param {number} x, 横坐标
     * @param {number} y, 纵坐标
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidPress(x, y){
        let angle = await this.getRotationAngle();
        let strRet = await AndroidBot.windowsBot.hidPress(this.androidId, angle, x, y);
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**移动
     * @param {number} x, 横坐标
     * @param {number} y, 纵坐标
     * @param {number} duration, 移动时长
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidMove(x, y, duration){
        let angle = await this.getRotationAngle();
        let strRet = await AndroidBot.windowsBot.hidMove(this.androidId, angle, x, y, duration);
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**释放
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidRelease(){
        let angle = await this.getRotationAngle();
        let strRet = await AndroidBot.windowsBot.hidRelease(this.androidId, angle);
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**单击
     * @param {number} x, 横坐标
     * @param {number} y, 纵坐标
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidClick(x, y){
        let angle = await this.getRotationAngle();
        let strRet = await AndroidBot.windowsBot.hidClick(this.androidId, angle, x, y);
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**双击
     * @param {number} x, 横坐标
     * @param {number} y, 纵坐标
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidDoubleClick(x, y){
        let angle = await this.getRotationAngle();
        let strRet = await AndroidBot.windowsBot.hidDoubleClick(this.androidId, angle, x, y);
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**长按
     * @param {number} x, 横坐标
     * @param {number} y, 纵坐标
     * @param {number} duration, 按下时长
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidLongClick(x, y, duration){
        let angle = await this.getRotationAngle();
        let strRet = await AndroidBot.windowsBot.hidLongClick(this.androidId, angle, x, y, duration);
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**滑动坐标
     * @param {number} startX, 起始横坐标
     * @param {number} startY, 起始纵坐标
     * @param {number} endX, 结束横坐标
     * @param {number} endY, 结束纵坐标
     * @param {number} duration, 滑动时长
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidSwipe(startX, startY, endX, endY, duration){
        let angle = await this.getRotationAngle();
        let strRet = await AndroidBot.windowsBot.hidSwipe(this.androidId, angle, startX, startY, endX, endY, duration);
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**Hid手势
     * @param {[[x:number, y:number], [x1:number, y1:number]...]} gesturePath 手势路径
     * @param {number} duration 手势时长，单位毫秒
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidDispatchGesture(gesturePath, duration){
        let angle = await this.getRotationAngle();
        let strRet = await AndroidBot.windowsBot.hidDispatchGesture(this.androidId, angle, gesturePath, duration);
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**Hid多手势
     * @param {[[duration:number, [x:number, y:number], [x1:number, y1:number]...],[duration:number, [x:number, y:number], [x1:number, y1:number]...],...]} gesturePaths  多点手势路径
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidDispatchGestures(gesturePaths){
        let angle = await this.getRotationAngle();
        let strRet = await AndroidBot.windowsBot.hidDispatchGestures(this.androidId, angle, gesturePaths);
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**hidBack
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidBack(){
        let strRet = await AndroidBot.windowsBot.hidBack(this.androidId);
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**hidHome
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidHome(){
        let strRet = await AndroidBot.windowsBot.hidHome(this.androidId);
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**hid显示最近任务
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidRecents(){
        let strRet = await AndroidBot.windowsBot.hidRecents(this.androidId);
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取手机旋转角度
     * @return {Promise.<number>} 返回手机旋转的角度
     */
    async getRotationAngle(){
        let strData = this.setSendData("getRotationAngle");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return parseInt(strRet);
    }

    /**关闭连接
     * @return {Promise.<void>}
     */
    async closeDriver(){
        let strData = this.setSendData("closeDriver");
        this.sendData(strData);
        return ;
    }

    /**激活框架
     * @param {string} activateKey, 激活密钥，联系管理员
     * @return {Promise.<boolean>} 返回激活信息
     */
    async activateFrame(activateKey){
        let strData = this.setSendData('activateFrame', activateKey);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }


    // /**特征匹配
    // * @param {string} imagePath 小图片路径（手机）
    // * @param {{region:[x, y, width, height]}} options 可选参数
    // * region 指定区域找图 [10, 20, 100, 200]，region默认全屏
    // * @return {{x:number, y:number} | null} 成功返回{x:number, y:number}，失败返回null
    // */
    //  async featureTemplate(imagePath, options = {}){
    //     let x = 0, y = 0, width = 0, height = 0;
    //     if(options["region"] != undefined){
    //         x = options["region"][0];
    //         y = options["region"][1];
    //         width = options["region"][2];
    //         height = options["region"][3];
    //     }

    //     let strData = this.setSendData("featureTemplate", imagePath, x, y, width, height);
    //     let strRet;
    //     let startTime = process.uptime() * 1000;
    //     let endTime = process.uptime() * 1000;
    //     do{
    //         strRet = await this.sendData(strData);
    //         if(strRet == "-1.0|-1.0")
    //             await this.sleep(this.intervalTimeout);
    //         else
    //             break;
    //         endTime = process.uptime() * 1000;
    //     }while(endTime - startTime <= this.waitTimeout);

    //     if(strRet == "-1.0|-1.0")
    //         return null;
    //     let arrRet = strRet.split("|");
    //     return {x: parseInt(arrRet[0]), y: parseInt(arrRet[1])};
    // }
}

module.exports = AndroidBot;