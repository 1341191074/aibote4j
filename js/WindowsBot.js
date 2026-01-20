const net = require('net');
const child_process = require('child_process');
const restler = require('restler');//验证码依赖
const fs = require('fs');
const path = require("path");
const os = require('os');

class WindowsBot{
    static server = [];
    /**
     * @param {function(WindowsBot)} windowsMain 要注册的函数，必须含一个参数，用作接收WindowsBot对象
     * @param {string} ip 脚本所在的地址，传递给WindowsDriver.exe。如果值为 "127.0.0.1"脚本会将参数 ip和port作为启动参数并启动WindowsDriver.exe，否则用户需要手动启动WindowsDriver.exe 并且提供启动参数。
     * @param {number} port 监听端口, 传递给WindowsDriver.exe
     * @param {string} driverFolder 驱动程序所在的文件夹，默认 "../"
     */
    static registerMain(windowsMain, ip, port, driverFolder = "../"){
        let driverName = 'WindowsDriver.exe';
        //win 7
        if(parseFloat(os.release()) < 10)
            driverName = "WindowsDriver_win7.exe";

        //本地ip由脚本服务端启动驱动程序
        if(ip == "127.0.0.1"){
            //获取驱动程序路径
            let driverPath = driverFolder + driverName;//path.resolve(__dirname, "../../" + driverName);
            let isExist = fs.existsSync(driverPath);
            if(!isExist)
                driverPath = driverName;

            child_process.execFile(driverPath, [ip, port]);
            console.log("正在启动WindowsDriver...");
        }

        let serverIndex = WindowsBot.server.length;
        WindowsBot.server[serverIndex] = new net.createServer();
        WindowsBot.server[serverIndex].listen(port);
        WindowsBot.server[serverIndex].on('connection', (clientSocket) => {
            windowsMain(new WindowsBot(clientSocket));
        });
    }

    socket;
    resolveHand;
    recvData;
    recvDataLen;
    isFirstData;
    waitTimeout;
    intervalTimeout;
    mutex;
    constructor(clientSocket) {
        this.socket = clientSocket;
        this.resolveHand = null;
        this.recvData = "";
        this.isFirstData = true;//标记write首次触发data事件
        this.waitTimeout = 0;//隐式等待超时
        this.intervalTimeout = 1;//每次等待的时间
        this.mutex = new Mutex();

        this.socket.on('connect', ()=>{
            this.resolveHand(this);//返回WindowsBot
        });

        this.socket.on('error', error=>{
            console.log(error);
        });

        this.socket.on('close', ()=>{
            console.log('WindowsBot已关闭');
        });

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

    static sleep(millisecond){
        return new Promise(resolve => {setTimeout(() => {resolve()}, millisecond)});
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


    //ocrByBinary 使用
    setSendFile = (functionName, imageBytes, left, top, right, bottom, thresholdType, thresh, maxval) =>{
        let strData = "";
        strData += Buffer.byteLength(functionName);
        strData += "/";
        strData += imageBytes.length;
        strData += "/";
        strData += Buffer.byteLength(left.toString());
        strData += "/";
        strData += Buffer.byteLength(top.toString());
        strData += "/";
        strData += Buffer.byteLength(right.toString());
        strData += "/";
        strData += Buffer.byteLength(bottom.toString());
        strData += "/";
        strData += Buffer.byteLength(thresholdType.toString());
        strData += "/";
        strData += Buffer.byteLength(thresh.toString());
        strData += "/";
        strData += Buffer.byteLength(maxval.toString());
        strData += "\n";

        strData += functionName;
        let byteData = Buffer.concat([Buffer.from(strData), imageBytes]);
        strData = "";
        strData += left;
        strData += top;
        strData += right;
        strData += bottom;
        strData += thresholdType;
        strData += thresh;
        strData += maxval;

        byteData = Buffer.concat([byteData, Buffer.from(strData)]);
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
     * @param {number} intervalMs 心跳间隔，单位毫秒。可选参数，默认10毫秒
     * @return {Promise.<void>}
     */
    async setImplicitTimeout(waitMs, intervalMs = 10){
        this.waitTimeout = waitMs;
        this.intervalTimeout = intervalMs;
    }

    /**查找窗口句柄
     * @param {string} className  窗口类名
     * @param {string} windowNmae 窗口名
     * @return {Promise.<string>} 成功返回窗口句柄，失败返回null
     */
    async findWindow(className, windowNmae){
        let strData = this.setSendData("findWindow", className, windowNmae);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**查找窗口句柄数组
     * @param {string} className  窗口类名
     * @param {string} windowNmae 窗口名
     * @return {Promise.<[]>} 成功返回窗口句柄数组，失败返回null
     */
    async findWindows(className, windowNmae){
        let strData = this.setSendData("findWindows", className, windowNmae);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet.split("|");
    }

    /**查找子窗口句柄
     * @param {string|number} curHwnd  当前窗口句柄
     * @param {string} className  窗口类名
     * @param {string} windowNmae 窗口名
     * @return {Promise.<string>} 成功返回窗口句柄，失败返回null
     */
    async findSubWindow(curHwnd, className, windowNmae){
        let strData = this.setSendData("findSubWindow", curHwnd, className, windowNmae);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**查找父窗口句柄
     * @param {string|number} curHwnd  当前窗口句柄
     * @return {Promise.<string>} 成功返回窗口句柄，失败返回null
     */
    async findParentWindow(curHwnd){
        let strData = this.setSendData("findParentWindow", curHwnd);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**查找桌面窗口句柄
     * @return {Promise.<string>} 成功返回窗口句柄，失败返回null
     */
    async findDesktopWindow(){
        let strData = this.setSendData("findDesktopWindow");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**获取窗口名称
     * @param {string|number} hwnd 窗口句柄
     * @return {Promise.<string>} 成功返回窗口名称，失败返回null
     */
    async getWindowName(hwnd){
        let strData = this.setSendData("getWindowName", hwnd);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**显示/隐藏窗口
     * @param {string|number} hwnd 窗口句柄
     * @param {boolean} isShow 显示窗口 true， 隐藏窗口 false
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async showWindow(hwnd, isShow){
        let strData = this.setSendData("showWindow", hwnd, isShow);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**设置窗口到最顶层
     * @param {string|number} hwnd 窗口句柄
     * @param {boolean} isTop 是否置顶，true置顶， false取消置顶
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async setWindowTop(hwnd, isTop){
        let strData = this.setSendData("setWindowTop", hwnd, isTop);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取窗口位置
     * @param {string|number} hwnd  窗口句柄
     * @return {Promise.<{left:number, top:number, width:number, height:number}>} 成功返回窗口位置，失败返回null
     */
    async getWindowPos(hwnd){
        let strData = this.setSendData('getWindowPos', hwnd);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "-1|-1|-1|-1")
            return null;
        let arrRet = strRet.split("|");
        return {left: parseInt(arrRet[0]), top: parseInt(arrRet[1]), width: parseInt(arrRet[2]), height: parseInt(arrRet[3])};
    }

    /**设置窗口位置
     * @param {string|number} hwnd  窗口句柄
     * @param {number} left 左上角横坐标
     * @param {number} top 左上角纵坐标
     * @param {number} width 窗口宽度
     * @param {number} height 窗口高度
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    async setWindowPos(hwnd, left, top, width, height){
        let strData = this.setSendData('setWindowPos', hwnd, left, top, width, height);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**移动鼠标
     * @param {string|number} hwnd  窗口句柄
     * @param {number} x  横坐标
     * @param {number} y  纵坐标
     * @param {{mode:boolean, elementHwnd:string|number}} options 操作模式，后台 true，前台 false。默认前台操作。
     * 如果mode值为true且目标控件有单独的句柄，则需要通过getElementWindow获得元素句柄，指定elementHwnd的值(新板本底层代码会自动获取子窗口句柄)
     * @return {Promise.<boolean>} 总是返回true
     */
    async moveMouse(hwnd, x, y, options = {}){
        let mode = false;
        let elementHwnd = 0;
        if(options["mode"] != undefined)
            mode = options["mode"];
        if(options["elementHwnd"] != undefined)
            elementHwnd = options["elementHwnd"];

        let strData = this.setSendData("moveMouse", hwnd, x, y, mode, elementHwnd);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return true;
    }

    /**移动鼠标(相对坐标)
     * @param {string|number} hwnd  窗口句柄
     * @param {number} x  相对横坐标
     * @param {number} y  相对纵坐标
     * @param {boolean} mode  操作模式，后台 true，前台 false。默认前台操作
     * @return {Promise.<boolean>} 总是返回true
     */
    async moveMouseRelative(hwnd, x, y, mode = false){
        let strData = this.setSendData("moveMouseRelative", hwnd, x, y, mode);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return true;
    }

    /**滚动鼠标
     * @param {string|number} hwnd  窗口句柄
     * @param {number} x  滚动前鼠标横坐标位置
     * @param {number} y  滚动前鼠标纵坐标位置
     * @param {number} dwData 鼠标滚动次数,负数下滚鼠标,正数上滚鼠标
     * @param {boolean} mode  操作模式，后台 true，前台 false。默认前台操作
     * @return {Promise.<boolean>} 总是返回true
     */
    async rollMouse(hwnd, x, y, dwData, mode = false){
        //先移动鼠标到目标位置
        await this.moveMouse(hwnd, x, y, mode);

        let strData = this.setSendData("rollMouse", hwnd, x, y, dwData, mode);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return true;
    }

    /**鼠标点击
     * @param {string|number} hwnd  窗口句柄
     * @param {number} x  横坐标
     * @param {number} y  纵坐标
     * @param {number} msg 单击左键:1 单击右键:2 按下左键:3 弹起左键:4 按下右键:5 弹起右键:6 双击左键:7 双击右键:8
     * @param {{mode:boolean, elementHwnd:string|number}} options 操作模式，后台 true，前台 false。默认前台操作。
     * 如果mode值为true且目标控件有单独的句柄，则需要通过getElementWindow获得元素句柄，指定elementHwnd的值(新板本底层代码会自动获取子窗口句柄)
     * @return {Promise.<boolean>} 总是返回true。
     */
    async clickMouse(hwnd, x, y, msg, options = {}){
        let mode = false;
        let elementHwnd = 0;
        if(options["mode"] != undefined)
            mode = options["mode"];
        if(options["elementHwnd"] != undefined)
            elementHwnd = options["elementHwnd"];

        let strData = this.setSendData("clickMouse", hwnd, x, y, msg, mode, elementHwnd);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return true;
    }

    /**输入文本
     * @param {string} text  输入的文本
     * @return {Promise.<boolean>} 总是返回true
     */
    async sendKeys(text){
        let strData = this.setSendData("sendKeys", text);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return true;
    }

    /**后台输入文本
     * @param {string|number} hwnd 窗口句柄，如果目标控件有单独的句柄，需要通过getElementWindow获得句柄
     * @param {string} text  输入的文本
     * @return {Promise.<boolean>} 总是返回true
     */
    async sendKeysByHwnd(hwnd, text){
        let strData = this.setSendData("sendKeysByHwnd", hwnd, text);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return true;
    }

    /**输入虚拟键值(VK)
     * @param {number} bVk VK键值，例如：回车对应 VK键值 13
     * @param {number} msg 按下弹起:1 按下:2 弹起:3
     * @return {Promise.<boolean>} 总是返回true
     */
    async sendVk(bVk, msg){
        let strData = this.setSendData("sendVk", bVk, msg);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return true;
    }

    /**后台输入虚拟键值(VK)
     * @param {string|number} hwnd  窗口句柄，如果目标控件有单独的句柄，需要通过getElementWindow获得句柄
     * @param {number} bVk VK键值，例如：回车对应 VK键值 13
     * @param {number} msg 按下弹起:1 按下:2 弹起:3
     * @return {Promise.<boolean>} 总是返回true。若是后台组合键，可使用sendVk 按下控制键(AltShiftCtrl...)，再组合其他按键
     */
    async sendVkByHwnd(hwnd, bVk, msg){
        let strData = this.setSendData("sendVkByHwnd", hwnd, bVk, msg);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return true;
    }

    /**截图保存
     * @param {string|number} hwnd  窗口句柄
     * @param {string} savePath 保存的位置
     * @param {{region:[left:number, top:number, right:number, bottom:number], threshold:[thresholdType:number, thresh:number, maxval:number], mode:boolean}} options 可选参数
     * region截图区域 [10, 20, 100, 200]，region默认  hwnd对应的窗口
     * threshold二值化图片, thresholdType算法类型：
     0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0
     1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva
     2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0
     3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变
     4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变
     5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值
     6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     thresh阈值，maxval最大值，threshold默认保存原图。thresh和maxval同为255时灰度处理
     * mode操作模式，后台 true，前台 false。默认前台操作
     * @return {Promise.<boolean>}
     */
    async saveScreenshot(hwnd, savePath, options = {}){
        let left = 0, top = 0, right = 0, bottom = 0;
        let thresholdType = 0, thresh = 0, maxval = 0;
        let mode = false;
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
        if(options["mode"] != undefined)
            mode = options["mode"];

        let strData = this.setSendData("saveScreenshot", hwnd, savePath, left, top, right, bottom, thresholdType, thresh, maxval, mode);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取指定坐标点的色值
     * @param {string|number} hwnd  窗口句柄
     * @param {number} x 横坐标
     * @param {number} y 纵坐标
     * @param {boolean} mode 操作模式，后台 true，前台 false。默认前台操作
     * @return {Promise.<string>} 成功返回#开头的颜色值，失败返回null
     */
    async getColor(hwnd, x, y, mode = false){
        let strData = this.setSendData("getColor", hwnd, x, y, mode);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**找图
     * @param {string|number} hwndOrBigImagePath  窗口句柄或者图片路径
     * @param {string} smallImagePath 小图片路径，多张小图查找应当用"|"分开小图路径
     * @param {{region:[left:number, top:number, right:number, bottom:number], sim:number, threshold:[thresholdType:number, thresh:number, maxval:number], multi:number, mode:boolean}} options 可选参数
     * region 指定区域找图 [10, 20, 100, 200]，region默认 hwnd对应的窗口
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
     * mode 操作模式，后台 true，前台 false。默认前台操作。hwndOrBigImagePath为图片文件，此参数无效
     * @return {Promise.<[{x:number, y:number}]>} 成功返回 单坐标点[{x:number, y:number}]，多坐标点[{x1:number, y1:number}, {x2:number, y2:number}...] 失败返回null
     */
    async findImage(hwndOrBigImagePath, smallImagePath, options = {}){
        //如果是文件名，这里添加默认路径
        const parsed = path.parse(smallImagePath);
        if (!parsed.dir){
            const findPictureDir = (maxDepth = 5)=>{
                let currentDir = process.cwd();
                let depth = 0;

                while (currentDir !== path.parse(currentDir).root && depth < maxDepth) {
                    const picturePath = path.join(currentDir, 'Picture');
                    if (fs.existsSync(picturePath) && fs.statSync(picturePath).isDirectory())
                        return picturePath;

                    // 向上一级目录
                    currentDir = path.dirname(currentDir);
                    depth++;
                }

                return null;
            }

            const picturePath = findPictureDir();
            if(picturePath != null)
                smallImagePath = path.join(picturePath, 'windows', smallImagePath);
        }

        let left = 0, top = 0, right = 0, bottom = 0;
        let sim = 0.95;
        let thresholdType = 0, thresh = 0, maxval = 0;
        let multi = 1;
        let mode = false;
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

        if(options["mode"] != undefined)
            mode = options["mode"];

        let strData;
        if(hwndOrBigImagePath.toString().indexOf(".") == -1)//在窗口上找图
            strData = this.setSendData("findImage", hwndOrBigImagePath, smallImagePath, left, top, right, bottom, sim, thresholdType, thresh, maxval, multi, mode);
        else//在文件上找图
            strData = this.setSendData("findImageByFile", hwndOrBigImagePath, smallImagePath, left, top, right, bottom, sim, thresholdType, thresh, maxval, multi, mode);
        let strRet, byteRet;
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "-1|-1")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "-1|-1")
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
     * @param {string|number} hwnd  窗口句柄
     * @param {number} frameRate 前后两张图相隔的时间，单位毫秒
     * @param {{region:[left:number, top:number, right:number, bottom:number], mode:boolean}} options 可选参数
     * region 指定区域找图 [10, 20, 100, 200]，region默认 hwnd对应的窗口
     * mode 操作模式，后台 true，前台 false。默认前台操作
     * @return {Promise.<[{x:number, y:number}]>} 成功返回 单坐标点[{x:number, y:number}]，多坐标点[{x1:number, y1:number}, {x2:number, y2:number}...] 失败返回null
     */
    async findAnimation(hwnd, frameRate, options = {}){
        let left = 0, top = 0, right = 0, bottom = 0;
        let mode = false;
        if(options["region"] != undefined){
            left = options["region"][0];
            top = options["region"][1];
            right = options["region"][2];
            bottom = options["region"][3];
        }

        if(options["mode"] != undefined)
            mode = options["mode"];

        let strData = this.setSendData("findAnimation", hwnd, frameRate, left, top, right, bottom, mode);
        let strRet, byteRet;
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "-1|-1")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "-1|-1")
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
     * @param {string|number} hwnd  窗口句柄
     * @param {string} strMainColor #开头的色值
     * @param {{subColors:[[offsetX:number, offsetY:number, strSubColor:string], ...], region:[left:number, top:number, right:number, bottom:number], sim:number, mode:boolean}} options 可选参数
     * subColors 相对于strMainColor 的子色值，[[offsetX, offsetY, "#FFFFFF"], ...]，subColors默认为null
     * region 指定区域找色 [10, 20, 100, 200]，region默认 hwnd对应的窗口
     * sim相似度0.0-1.0，sim默认为0.98
     * mode 操作模式，后台 true，前台 false。默认前台操作
     * @return {Promise.<{x:number, y:number}>} 成功返回{x:number, y:number} 失败返回null
     */
    async findColor(hwnd, strMainColor, options = {}){
        let strSubColors = "null";
        let left = 0, top = 0, right = 0, bottom = 0;
        let sim = 0.98;
        let mode = false;
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
        if(options["mode"] != undefined)
            mode = options["mode"];

        let strData = this.setSendData("findColor", hwnd, strMainColor, strSubColors, left, top, right, bottom, sim, mode);
        let strRet, byteRet;
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "-1|-1")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "-1|-1")
            return null;
        let arrRet = strRet.split("|");
        return {x: parseInt(arrRet[0]), y: parseInt(arrRet[1])};
    }

    /**比较指定坐标点的颜色值
     * @param {string|number} hwnd  窗口句柄
     * @param {number} mainX 主颜色所在的X坐标
     * @param {number} mainY 主颜色所在的Y坐标
     * @param {string} strMainColor #开头的色值
     * @param {{subColors:[[offsetX:number, offsetY:number, strSubColor:string], ...], region:[left:number, top:number, right:number, bottom:number], sim:number, mode:boolean}} options 可选参数
     * subColors 相对于strMainColor 的子色值，[[offsetX, offsetY, "#FFFFFF"], ...]，subColors默认为null
     * region 指定区域找色 [10, 20, 100, 200]，region默认 hwnd对应的窗口
     * sim相似度0.0-1.0，sim默认为0.98
     * mode 操作模式，后台 true，前台 false。默认前台操作
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    async compareColor(hwnd, mainX, mainY, strMainColor, options = {}){
        let strSubColors = "null";
        let left = 0, top = 0, right = 0, bottom = 0;
        let sim = 0.98;
        let mode = false;
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
        if(options["mode"] != undefined)
            mode = options["mode"];

        let strData = this.setSendData("compareColor", hwnd, mainX, mainY, strMainColor, strSubColors, left, top, right, bottom, sim, mode);
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

    /**提取视频帧
     * @param {string} videoPath 视频路径
     * @param {string} saveFolder 提取的图片保存的文件夹目录
     * @param {number} jumpFrame 跳帧，默认为1 不跳帧
     * @return {Promise.<boolean>}成功返回true，失败返回false
     */
    async extractImageByVideo(videoPath, saveFolder, jumpFrame = 1){
        let strData = this.setSendData("extractImageByVideo", videoPath, saveFolder, jumpFrame);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**裁剪图片
     * @param {string} imagePath 图片路径
     * @param {string} savePath 裁剪后保存的图片路径
     * @param {number} left 裁剪的左上角横坐标
     * @param {number} top 裁剪的左上角纵坐标
     * @param {number} rigth 裁剪的右下角横坐标
     * @param {number} bottom 裁剪的右下角纵坐标
     * @return {Promise.<boolean>}成功返回true，失败返回false
     */
    async cropImage(imagePath, savePath, left, top, rigth, bottom){
        let strData = this.setSendData("cropImage", imagePath, savePath, left, top, rigth, bottom);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**ocrByHwnd
     * @param {string} ocrServerIp ocr服务端IP，端口固定为9527。
     * @param {string|number} hwnd  窗口句柄
     * @param {left:number} left 左上角x点
     * @param {top:number} top 左上角y点
     * @param {right:number} right 右下角 x点
     * @param {bottom:number} bottom 右下角 y点
     * @param {thresholdType:number} thresholdType 二值化算法类型
     * @param {thresh:number} thresh 阈值
     * @param {maxval:number} maxval 最大值
     * @param {mode:boolean} mode 操作模式，后台 true，前台 false。默认前台操作
     * @return {Promise.<[]>} 失败返回null，成功返回数组形式的识别结果
     */
    ocrByHwnd = async (ocrServerIp, hwnd, left, top, right, bottom, thresholdType, thresh, maxval, mode = false) =>{
        let strData = this.setSendData("ocrByHwnd", ocrServerIp, hwnd, left, top, right, bottom, thresholdType, thresh, maxval, mode);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == '' || strRet == "null" || strRet == "[]")
            return null;
        else
            return JSON.parse(strRet);
    }

    /**ocrByFile
     * @param {string} ocrServerIp ocr服务端IP，端口固定为9527。
     * @param {string} imagePath 图片路径
     * @param {left:number} left 左上角x点
     * @param {top:number} top 左上角y点
     * @param {right:number} right 右下角 x点
     * @param {bottom:number} bottom 右下角 y点
     * @param {thresholdType:number} thresholdType 二值化算法类型
     * @param {thresh:number} thresh 阈值
     * @param {maxval:number} maxval 最大值
     * @return {Promise.<[]>} 失败返回null，成功返回数组形式的识别结果
     */
    ocrByFile = async (ocrServerIp, imagePath, left, top, right, bottom, thresholdType, thresh, maxval) =>{
        let strData = this.setSendData("ocrByFile", ocrServerIp, imagePath, left, top, right, bottom, thresholdType, thresh, maxval);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null" || strRet == "[]")
            return null;
        else
            return JSON.parse(strRet);
    }

    /**获取屏幕文字
     * @param {string} ocrServerIp ocr服务端IP，端口固定为9527。
     * @param {string|number} hwndOrFile  窗口句柄或者图片路径
     * @param {{region:[left:number, top:number, right:number, bottom:number], threshold:[thresholdType:number, thresh:number, maxval:number], mode:boolean}} options 可选参数
     * region 指定区域 [10, 20, 100, 200]，region默认全图
     * threshold二值化图片, thresholdType算法类型：
     0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0
     1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva
     2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0
     3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变
     4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变
     5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值
     6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     thresh阈值，maxval最大值，threshold默认保存原图。thresh和maxval同为255时灰度处理
     * mode 操作模式，后台 true，前台 false。默认前台操作, 仅适用于hwnd
     * @return {Promise.<[string]>} 失败返回null，成功以数组字符串的形式返窗口上的文字
     */
    async getWords(ocrServerIp, hwndOrFile, options = {}){
        let left = 0, top = 0, right = 0, bottom = 0;
        if(options["region"] != undefined){
            left = options["region"][0];
            top = options["region"][1];
            right = options["region"][2];
            bottom = options["region"][3];
        }

        let thresholdType = 0, thresh = 0, maxval = 0;
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

        let wordsResult;
        if(typeof(hwndOrFile) == 'number' || hwndOrFile.indexOf(".") == -1){//hwnd
            let mode = false;
            if(options["mode"] != undefined)
                mode = options["mode"];
            wordsResult = await this.ocrByHwnd(ocrServerIp, hwndOrFile, left, top, right, bottom, thresholdType, thresh, maxval, mode);
        }else//file
            wordsResult = await this.ocrByFile(ocrServerIp, hwndOrFile, left, top, right, bottom, thresholdType, thresh, maxval);

        if(wordsResult == null)
            return null;

        const words = wordsResult.map(item => item.text);
        return words;
    }

    /**查找文字
     * @param {string} ocrServerIp ocr服务端IP，端口固定为9527。
     * @param {string|number} hwndOrFile 窗口句柄或者图片路径
     * @param {string} words 要查找的文字
     * @param {{region:[left:number, top:number, right:number, bottom:number], threshold:[thresholdType:number, thresh:number, maxval:number], mode:boolean}} options 可选参数
     * region 指定区域 [10, 20, 100, 200]，region默认全图
     * threshold二值化图片, thresholdType算法类型：
     0   THRESH_BINARY算法，当前点值大于阈值thresh时，取最大值maxva，否则设置为0
     1   THRESH_BINARY_INV算法，当前点值大于阈值thresh时，设置为0，否则设置为最大值maxva
     2   THRESH_TOZERO算法，当前点值大于阈值thresh时，不改变，否则设置为0
     3   THRESH_TOZERO_INV算法，当前点值大于阈值thresh时，设置为0，否则不改变
     4   THRESH_TRUNC算法，当前点值大于阈值thresh时，设置为阈值thresh，否则不改变
     5   ADAPTIVE_THRESH_MEAN_C算法，自适应阈值
     6   ADAPTIVE_THRESH_GAUSSIAN_C算法，自适应阈值
     thresh阈值，maxval最大值，threshold默认保存原图。thresh和maxval同为255时灰度处理
     * mode 操作模式，后台 true，前台 false。默认前台操作, 仅适用于hwnd
     * @return {Promise.<[{x:number, y:number}]>} 失败返回null，成功返回数组[{x:number, y:number}, ...]，文字所在的坐标点
     */
    async findWords(ocrServerIp, hwndOrFile, words, options = {}){
        let left = 0, top = 0, right = 0, bottom = 0;
        if(options["region"] != undefined){
            left = options["region"][0];
            top = options["region"][1];
            right = options["region"][2];
            bottom = options["region"][3];
        }

        let thresholdType = 0, thresh = 0, maxval = 0;
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

        let wordsResult;
        if(typeof(hwndOrFile) == 'number' || hwndOrFile.indexOf(".") == -1){//hwnd
            let mode = false;
            if(options["mode"] != undefined)
                mode = options["mode"];
            wordsResult = await this.ocrByHwnd(ocrServerIp, hwndOrFile, left, top, right, bottom, thresholdType, thresh, maxval, mode);
        }else//file
            wordsResult = await this.ocrByFile(ocrServerIp, hwndOrFile, left, top, right, bottom, thresholdType, thresh, maxval);

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
            let x = parseInt(localLeft + offsetX + left);
            let y = parseInt(localTop + offsetY + top);
            points[i] = {"x":x, "y":y};
        }

        if(points.length == 0)
            return null;
        else
            return points;
    }

    /**yoloByHwnd
     * @param {string} yoloServerIp yolo服务端IP，端口固定为9528。
     * @param {string|number} hwnd  窗口句柄
     * @param {left:number} left 左上角x点
     * @param {top:number} top 左上角y点
     * @param {right:number} right 右下角 x点
     * @param {bottom:number} bottom 右下角 y点
     * @param {mode:boolean} mode 操作模式，后台 true，前台 false。默认前台操作
     * @return {Promise.<[]>} 失败返回null，成功返回数组形式的识别结果
     */
    yoloByHwnd = async (yoloServerIp, hwnd, left, top, right, bottom, mode) =>{
        let strData = this.setSendData("yoloByHwnd", yoloServerIp, hwnd, left, top, right, bottom, mode);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == '' || strRet == "null" || strRet == "[]")
            return null;
        else
            return JSON.parse(strRet);
    }

    /**yoloByFile
     * @param {string} yoloServerIp yolo服务端IP，端口固定为9528。
     * @param {string} imagePath 图片路径
     * @param {left:number} left 左上角x点
     * @param {top:number} top 左上角y点
     * @param {right:number} right 右下角 x点
     * @param {bottom:number} bottom 右下角 y点
     * @return {Promise.<[]>} 失败返回null，成功返回数组形式的识别结果
     */
    yoloByFile = async (yoloServerIp, imagePath, left, top, right, bottom) =>{
        let strData = this.setSendData("yoloByFile", yoloServerIp, imagePath, left, top, right, bottom);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == '' || strRet == "null" || strRet == "[]")
            return null;
        else
            return JSON.parse(strRet);
    }

    /**yolo
     * @param {string} yoloServerIp yolo服务端IP，端口固定为9528。
     * @param {string|number} hwndOrFile  窗口句柄或者图片路径
     * @param {{region:[left:number, top:number, right:number, bottom:number], mode:boolean}} options 可选参数
     * region 指定区域 [10, 20, 100, 200]，region默认全图。区域设置应当和训练时区域一致
     * mode 操作模式，后台 true，前台 false。默认前台操作, 仅适用于hwnd，文件识别会自动忽略此参数
     * @return {Promise.<[JSON]>} 失败返回null，成功返回数组形式的识别结果。
     */
    async yolo(yoloServerIp, hwndOrFile, options = {}){
        let left = 0, top = 0, right = 0, bottom = 0;
        if(options["region"] != undefined){
            left = options["region"][0];
            top = options["region"][1];
            right = options["region"][2];
            bottom = options["region"][3];
        }

        let mode = false;
        if(options["mode"] != undefined)
            mode = options["mode"];

        let result;
        if(typeof(hwndOrFile) == 'number' || hwndOrFile.indexOf(".") == -1){//hwnd
            result = await this.yoloByHwnd(yoloServerIp, hwndOrFile, left, top, right, bottom, mode);
        }else//file
            result = await this.yoloByFile(yoloServerIp, hwndOrFile, left, top, right, bottom);

        return result;
    }

    /**获取指窗口所有可见元素
     * @param {string|number} hwnd  窗口句柄
     * @param {string|null} ocrServerIp ocr服务端IP，端口固定为9527，默认为 null。若提供此参数，则返回结果增加 words 字段，包含界面中所有的文字区域
     * @return {Promise.<{elements:[{}]}|{elements:[{}], words:[{}]}|null>} 成功返回json格式的元素信息，失败返回null
     */
    async getElements(hwnd, ocrServerIp = null){
        let strData = this.setSendData("getElements", hwnd);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        try{
            let elementsJson = JSON.parse(strRet);
            if(ocrServerIp != null &&ocrServerIp != ''){
                let words = await this.ocrByHwnd(ocrServerIp, hwnd, 0, 0, 0, 0, 0, 0, 0, true);
                elementsJson["words"] = words;
            }
            return elementsJson;
        }catch(e){
            return null;
        }
    }

    /**获取指定元素名称
     * @param {string|number} hwnd  窗口句柄。如果是java窗口并且窗口句柄和元素句柄不一致，需要使用getElementWindow获取窗口句柄。
     * getElementWindow参数的xpath，Aibote Tool应当使用正常模式下获取的XPATH路径，不要 “勾选java窗口” 复选按钮。对话框子窗口，需要获取对应的窗口句柄操作
     * @param {string} xpath 元素路径
     * @return {Promise.<string>} 成功返回元素名称
     */
    async getElementName(hwnd, xpath){
        let strData = this.setSendData('getElementName', hwnd, xpath);
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let strRet, byteRet;
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

    /**获取指定元素文本
     * @param {string|number} hwnd  窗口句柄
     * @param {string} xpath 元素路径
     * @return {Promise.<string>} 成功返回元素文本
     */
    async getElementValue(hwnd, xpath){
        let strData = this.setSendData('getElementValue', hwnd, xpath);
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let strRet, byteRet;
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

    /**获取指定元素矩形大小
     * @param {string|number} hwnd  窗口句柄。如果是java窗口并且窗口句柄和元素句柄不一致，需要使用getElementWindow获取窗口句柄。
          * getElementWindow参数的xpath，Aibote Tool应当使用正常模式下获取的XPATH路径，不要 “勾选java窗口” 复选按钮。对话框子窗口，需要获取对应的窗口句柄操作
     * @param {string} xpath 元素路径
     * @return {Promise.<{left:number, top:number, right:number, bottom:number}>} 成功返回元素位置，失败返回null
     */
    async getElementRect(hwnd, xpath){
        let strData = this.setSendData('getElementRect', hwnd, xpath);
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let strRet, byteRet;
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

    /**获取元素窗口句柄
     * @param {string|number} hwnd  窗口句柄
     * @param {string} xpath 元素路径
     * @return {Promise.<string>} 成功返回元素窗口句柄，失败返回null
     */
    async getElementWindow(hwnd, xpath){
        let strData = this.setSendData('getElementWindow', hwnd, xpath);
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let strRet, byteRet;
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

    /**点击元素
     * @param {string|number} hwnd  窗口句柄。如果是java窗口并且窗口句柄和元素句柄不一致，需要使用getElementWindow获取窗口句柄。
          * getElementWindow参数的xpath，Aibote Tool应当使用正常模式下获取的XPATH路径，不要 “勾选java窗口” 复选按钮。对话框子窗口，需要获取对应的窗口句柄操作
     * @param {string} xpath 元素路径
     * @param {number} msg 单击左键:1 单击右键:2 按下左键:3 弹起左键:4 按下右键:5 弹起右键:6 双击左键:7 双击右键:8
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    async clickElement(hwnd, xpath, msg){
        let strData = this.setSendData('clickElement', hwnd, xpath, msg);
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let strRet, byteRet;
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

    /**执行元素默认操作(一般是点击操作)
     * @param {string|number} hwnd  窗口句柄。
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    async invokeElement(hwnd, xpath){
        let strData = this.setSendData('invokeElement', hwnd, xpath);
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let strRet, byteRet;
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

    /**设置指定元素作为焦点
     * @param {string|number} hwnd  窗口句柄
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    async setElementFocus(hwnd, xpath){
        let strData = this.setSendData('setElementFocus', hwnd, xpath);
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let strRet, byteRet;
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

    /**设置元素文本
     * @param {string|number} hwnd  窗口句柄。如果是java窗口并且窗口句柄和元素句柄不一致，需要使用getElementWindow获取窗口句柄。
          * getElementWindow参数的xpath，Aibote Tool应当使用正常模式下获取的XPATH路径，不要 “勾选java窗口” 复选按钮。对话框子窗口，需要获取对应的窗口句柄操作
     * @param {string} xpath 元素路径
     * @param {string} value 要设置的内容
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    async setElementValue(hwnd, xpath, value){
        let strData = this.setSendData('setElementValue', hwnd, xpath, value);
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let strRet, byteRet;
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
     * @param {string|number} hwnd  窗口句柄
     * @param {string} xpath 元素路径
     * @param {number} horizontalPercent 水平百分比 -1不滚动
     * @param {number} verticalPercent 垂直百分比 -1不滚动。例如设置50，则垂直方向滚动到中间
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    async setElementScroll(hwnd, xpath, horizontalPercent, verticalPercent){
        let strData = this.setSendData('setElementScroll', hwnd, xpath, horizontalPercent, verticalPercent);
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let strRet, byteRet;
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

    /**单/复选框是否选中
     * @param {string|number} hwnd  窗口句柄
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    async isSelected(hwnd, xpath){
        let strData = this.setSendData('isSelected', hwnd, xpath);
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let strRet, byteRet;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "false")
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        if(strRet == "selected")
            return true;
        else
            return false;
    }

    /**关闭窗口
     * @param {string|number} hwnd  窗口句柄。如果是java窗口并且窗口句柄和元素句柄不一致，需要使用getElementWindow获取窗口句柄。
          * getElementWindow参数的xpath，Aibote Tool应当使用正常模式下获取的XPATH路径，不要 “勾选java窗口” 复选按钮。对话框子窗口，需要获取对应的窗口句柄操作
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    async closeWindow(hwnd, xpath){
        let strData = this.setSendData('closeWindow', hwnd, xpath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**设置窗口状态
     * @param {string|number} hwnd  窗口句柄。如果是java窗口并且窗口句柄和元素句柄不一致，需要使用getElementWindow获取窗口句柄。
          * getElementWindow参数的xpath，Aibote Tool应当使用正常模式下获取的XPATH路径，不要 “勾选java窗口” 复选按钮。对话框子窗口，需要获取对应的窗口句柄操作
     * @param {string} xpath 元素路径
     * @param {number} state 0正常 1最大化 2 最小化
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    async setWindowState(hwnd, xpath, state){
        let strData = this.setSendData('setWindowState', hwnd, xpath, state);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**设置剪切板文本
     * @param {string} text 设置的文本
     * @return {Promise.<boolean>} 成功返回true 失败返回 false
     */
    async setClipboardText(text){
        let strData = this.setSendData('setClipboardText', text);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取剪切板文本
     * @return {Promise.<string>} 返回剪切板文本
     */
    async getClipboardText(){
        let strData = this.setSendData('getClipboardText');
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**启动指定程序
     * @param {string} commandLine 启动命令行
     * @param {boolean} showWindow 是否显示窗口。可选参数,默认显示窗口
     * @param {boolean} isWait 是否等待程序结束。可选参数,默认不等待
     * @return {Promise.<boolean>} 成功返回true,失败返回false
     */
    async startProcess(commandLine, showWindow = true, isWait = false){
        let strData = this.setSendData('startProcess', commandLine, showWindow, isWait);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**执行cmd命令
     * @param {string} command cmd命令，不能含 "cmd"字串
     * @param {number} waitTimeout 可选参数，等待结果返回超时，单位毫秒，默认300毫秒
     * @return {Promise.<string>} 返回cmd执行结果
     */
    async executeCommand(command, waitTimeout = 300){
        let strData = this.setSendData('executeCommand', command, waitTimeout);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**指定url下载文件
     * @param {string} url 文件地址
     * @param {string} filePath 文件保存的路径
     * @param {boolean} isWait 是否等待.为true时,等待下载完成
     * @return {Promise.<boolean>} 总是返回true
     */
    async downloadFile(url, filePath, isWait){
        let strData = this.setSendData('downloadFile', url, filePath, isWait);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return true;
    }

    /**打开excel文档
     * @param {string} excelPath excle路径
     * @return {Promise.<Object>} 成功返回excel对象，失败返回null
     */
    async openExcel(excelPath){
        let strData = this.setSendData('openExcel', excelPath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return JSON.parse(strRet);
    }

    /**打开excel表格
     * @param {Object} excelObject excel对象
     * @param {string} sheetName 表名
     * @return {Promise.<Object>} 成功返回sheet对象，失败返回null
     */
    async openExcelSheet(excelObject, sheetName){
        let strData = this.setSendData('openExcelSheet', excelObject['book'], excelObject['path'], sheetName);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**保存excel文档
     * @param {Object} excelObject excel对象
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async saveExcel(excelObject){
        let strData = this.setSendData('saveExcel', excelObject['book'], excelObject['path']);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**写入数字到excel表格
     * @param {Object} sheetObject sheet对象
     * @param {number} row 行
     * @param {number} col 列
     * @param {number} value 写入的值
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async writeExcelNum(sheetObject, row, col, value){
        let strData = this.setSendData('writeExcelNum', sheetObject, row, col, value);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**写入字符串到excel表格
     * @param {Object} sheetObject sheet对象
     * @param {number} row 行
     * @param {number} col 列
     * @param {string} strValue 写入的值
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async writeExcelStr(sheetObject, row, col, strValue){
        let strData = this.setSendData('writeExcelStr', sheetObject, row, col, strValue);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**读取excel表格数字
     * @param {Object} sheetObject sheet对象
     * @param {number} row 行
     * @param {number} col 列
     * @return {Promise.<number>} 返回读取到的数字
     */
    async readExcelNum(sheetObject, row, col){
        let strData = this.setSendData('readExcelNum', sheetObject, row, col);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return parseFloat(strRet);
    }

    /**读取excel表格字串
     * @param {Object} sheetObject sheet对象
     * @param {number} row 行
     * @param {number} col 列
     * @return {Promise.<string>} 返回读取到的字符
     */
    async readExcelStr(sheetObject, row, col){
        let strData = this.setSendData('readExcelStr', sheetObject, row, col);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**删除excel表格行
     * @param {Object} sheetObject sheet对象
     * @param {number} rowFirst 起始行
     * @param {number} rowLast 结束行
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async removeExcelRow(sheetObject, rowFirst, rowLast){
        let strData = this.setSendData('removeExcelRow', sheetObject, rowFirst, rowLast);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**删除excel表格列
     * @param {Object} sheetObject sheet对象
     * @param {number} colFirst 起始列
     * @param {number} colLast 结束列
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async removeExcelCol(sheetObject, colFirst, colLast){
        let strData = this.setSendData('removeExcelCol', sheetObject, colFirst, colLast);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
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
        let fileBase64 = await fs.readFileSync(filePath, 'base64');
        return new Promise((resolve) => {
            restler.post('http://upload.chaojiying.net/Upload/Processing.php', {
                multipart: true,
                data: {
                    'user': username,
                    'pass': password,
                    'softid':softId,
                    'codetype': codeType,
                    'len_min':lenMin,
                    'file_base64': fileBase64
                },
                headers: {
                    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:24.0) Gecko/20100101 Firefox/24.0',
                    'Content-Type' : 'application/x-www-form-urlencoded'
                }
            }).on('complete', function(data) {
                resolve(data);
            });
        });
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
        return new Promise((resolve) => {
            restler.post('http://upload.chaojiying.net/Upload/ReportError.php', {
                multipart: true,
                data: {
                    'user': username,
                    'pass': password,
                    'softid':softId,
                    'id': picId,
                },
                headers: {
                    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:24.0) Gecko/20100101 Firefox/24.0',
                    'Content-Type' : 'application/x-www-form-urlencoded'
                }
            }).on('complete', function(data) {
                resolve(data);
            });
        });
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
        return new Promise((resolve) => {
            restler.post('http://upload.chaojiying.net/Upload/GetScore.php', {
                multipart: true,
                data: {
                    'user': username,
                    'pass': password,
                },
                headers: {
                    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:24.0) Gecko/20100101 Firefox/24.0',
                    'Content-Type' : 'application/x-www-form-urlencoded'
                }
            }).on('complete', function(data) {
                resolve(data);
            });
        });
    }

    /**初始化NLP
     * @param {string} aipKey 密钥
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async initNLP(aipKey){
        let strData = this.setSendData("initNLP", aipKey);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**chatGpt
     * @param {string} model, 指定使用的模型,"gpt-3.5-turbo"、text-davinci-003"、"text-curie-001"、"text-babbage-001"、"text-ada-001"和自定义微调模型
     * @param {string} promptOrMessages, 提出的问题，当model = "gpt-3.5-turbo"时，提问格式为 '[{"role": "user", "content": "你好！"}]' 。role 角色，content 问题内容
     * @param {number} maxTokens, 最大令牌数，大约 3个字符1个令牌，1个汉字2个令牌
     * @param {number} temperature, 浮点型，温度，调节结果的创意程度，0一般为单一结果， 1创意度更高
     * @param {string} stop, 可选参数，停止结果输出标志，一般用在微调模型上，例如 stop = '["END"]'
     * @return {Promise.<{text:string, finish:boolean} || null>} 失败返回null, 成功返回json对象
     * text 返回的答案内容
     * finish 为true回答结束，false 还有未输出的答案。我们可以继续 promptOrMessages + 输出的答案 获取后续内容，直到finish为true
     */
    async chatgpt(model, promptOrMessages, maxTokens, temperature, stop = ""){
        let strData = this.setSendData("chatgpt", model, promptOrMessages, maxTokens, temperature, stop);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return JSON.parse(strRet);
    }

    /**chatGpt编辑模式
     * @param {string} model, 指定使用的模型， "text-davinci-edit-001"、"code-davinci-edit-001"
     * @param {string} input, 输入要编辑的内容
     * @param {string} instruction, 提示如何去编辑
     * @param {number} maxTokens, 最大令牌数，大约 3个字符1个令牌，1个汉字2个令牌
     * @param {number} temperature, 浮点型，温度，调节结果的创意程度，0一般为单一结果， 1创意度更高
     * @return {Promise.<{text:string, finish:boolean} || null>} 失败返回null, 成功返回json对象
     * text 返回的答案内容
     * finish 为true回答结束，false 还有未输出的答案。我们可以继续 prompt + 输出的答案 获取后续内容，直到finish为true
     */
    async chatgptEdit(model, input, instruction, maxTokens, temperature){
        let strData = this.setSendData("chatgptEdit", model, input, instruction, maxTokens, temperature);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return JSON.parse(strRet);
    }

    /**创建微调模型
     * @param {string} fileId, 文件id，可通过 uploadTrainFile函数上传并获取文件id
     * @param {string} baseModel, 基础模型，可以是以下参数之一，"ada", "babbage", "curie", "davinci"和自定义微调模型
     * @param {string} suffix, 微调生成的模型名称后缀
     * @return {Promise.<string>} 成功返回微调id，失败返回null
     */
    async createFineTune(fileId, baseModel, suffix){
        let strData = this.setSendData("createFineTune", fileId, baseModel, suffix);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**列出所有微调信息
     * @return {Promise.<{[{baseModel:string, object:string, fineTuneId:string, fineTunedModel:string, fineTuneStatus:string, fileName:string, fileId:strinig, fileStatus:string}, ...] || null}>}
     * 成功返回json对象 数组，失败返回null
     * baseModel 基础模型，一般是"ada", "babbage", "curie", "davinci"
     * object
     * fineTuneId 微调id
     * fineTunedModel 微调模型的名称
     * fineTuneStatus 正在微调模型的进度状态
     * fileName 训练数据文件的名称
     * fileId 训练数据文件的id
     * fileStatus 训练数据文件的状态
     */
    async listFineTunes(){
        let strData = this.setSendData("listFineTunes");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return JSON.parse(strRet);
    }

    /**获取指定微调id的详细信息
     * @param {string} fineTuneId, 微调id
     * @return {Promise.<{baseModel:string, fineTuneCosts:string, fineTunedModel:string, fineTuneStatus:string, fileName:string, fileId:string,  fileStatus:string} || null>}
     * 成功返回json对象，失败返回null
     * baseModel 基础模型，一般是"ada", "babbage", "curie", "davinci"
     * fineTuneCosts 训练该模型消耗的$
     * fineTunedModel 微调模型的名称
     * fineTuneStatus 正在微调模型的进度状态
     * fileName 训练数据文件的名称
     * fileId 训练数据文件的id
     * fileStatus 训练数据文件的状态
     */
    async listFineTune(fineTuneId){
        let strData = this.setSendData('listFineTune', fineTuneId);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return JSON.parse(strRet);
    }

    /**取消正在微调的作业
     * @param {string} fineTuneId, 微调id
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async cancelFineTune(fineTuneId){
        let strData = this.setSendData('cancelFineTune', fineTuneId);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**删除微调模型
     * @param {string} fineTuneId, 微调id
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async deleteFineTuneModel(fineTuneId){
        let strData = this.setSendData('deleteFineTuneModel', fineTuneId);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**上传训练文件到服务器
     * @param {string} filePath, 文件路径，不支持中文路径
     * @return {Promise.<string>} 成功返回文件id，失败返回null
     */
    async uploadTrainFile(filePath){
        let strData = this.setSendData('uploadTrainFile', filePath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**列出所有训练文件信息
     * @return {Promise.<[{bytes:number, fileName:string, fileId:string, purpose:string}] || null>} 成功返回json对象 数组，失败返回null
     * bytes 训练数据文件的大小
     * fileName 训练数据文件的名称
     * fileId 训练数据文件的id
     * purpose 文件的意图 例如："fine-tune" 意图为 用作微调模型
     */
    async listTrainFiles(){
        let strData = this.setSendData('listTrainFiles');
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return JSON.parse(strRet);
    }

    /**列出指定id的文件信息
     * @param {string} fileId, 文件id
     * @return {Promise.<{bytes:number, fileName:string, fileId:string, purpose:string} || null>} 成功返回json对象，失败返回null
     * bytes 训练数据文件的大小
     * fileName 训练数据文件的名称
     * fileId 训练数据文件的id
     * purpose 文件的意图 例如："fine-tune" 意图为 用作微调模型
     */
    async listTrainFile(fileId){
        let strData = this.setSendData('listTrainFile', fileId);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return JSON.parse(strRet);
    }

    /**下载训练文件内容
     * @param {string} fileId, 文件id
     * @return {Promise.<string>} 成功返回文件内容，失败返回null
     */
    async downloadTrainFile(fileId){
        let strData = this.setSendData('downloadTrainFile', fileId);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**删除训练文件
     * @param {string} fileId, 文件id
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async deleteTrainFile(fileId){
        let strData = this.setSendData('deleteTrainFile', fileId);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**初始化语音服务(不支持win7)
     * @param {string} speechKey, 微软语音API密钥
     * @param {string} speechRegion, 区域
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async initSpeechService(speechKey, speechRegion){
        let strData = this.setSendData('initSpeechService', speechKey, speechRegion);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**音频文件转文本
     * @param {string} filePath, 音频文件路径
     * @param {string} language, 语言，参考开发文档 语言和发音人
     * @return {Promise.<string || null>} 成功返回转换后的音频文本，失败返回null
     */
    async audioFileToText(filePath, language){
        let strData = this.setSendData('audioFileToText', filePath, language);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**麦克风输入流转换文本
     * @param {string} language, 语言，参考开发文档 语言和发音人
     * @return {Promise.<string || null>} 成功返回转换后的音频文本，失败返回null
     */
    async microphoneToText(language){
        let strData = this.setSendData('microphoneToText', language);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**文本合成音频到扬声器
     * @param {string} ssmlPathOrText，要转换语音的文本或者".xml"格式文件路径
     * @param {string} language，语言，参考开发文档 语言和发音人
     * @param {string} voiceName，发音人，参考开发文档 语言和发音人
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async textToBullhorn(ssmlPathOrText, language, voiceName){
        let strData = this.setSendData("textToBullhorn", ssmlPathOrText, language, voiceName);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**文本合成音频并保存到文件
     * @param {string} ssmlPathOrText，要转换语音的文本或者".xml"格式文件路径
     * @param {string} language，语言，参考开发文档 语言和发音人
     * @param {string} voiceName，发音人，参考开发文档 语言和发音人
     * @param {string} audioPath，保存音频文件路径
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async textToAudioFile(ssmlPathOrText, language, voiceName, audioPath){
        let strData = this.setSendData("textToAudioFile", ssmlPathOrText, language, voiceName, audioPath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**文本生成语音文件和lab文件
     * @param {string} saveAudioPath, 保存的音频文件路径，扩展为.MP3格式。同名的 .lab文件会和音频文件生成在同一目录下
     * @param {string} text,要转换语音的文本
     * @param {string} language，语言，参考开发文档 语言和发音人
     * @param {string} voiceName，发音人，参考开发文档 语言和发音人
     * @param {number} quality，音质，0低品质  1中品质  2高品质， 默认为0低品质
     * @param {number} speechRate， 语速，默认为0，取值范围 -100 至 200
     * @param {string} voiceStyle，语音风格，默认General常规风格，其他风格参考开发文档 语言和发音人
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async textToAudioAndLabFile(saveAudioPath, text, language, voiceName, quality = 0, speechRate = 0, voiceStyle = "General"){
        let strData = this.setSendData("textToAudioAndLabFile", saveAudioPath, text, language, voiceName, quality, speechRate, voiceStyle);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**麦克风音频翻译成目标语言文本
     * @param {string} sourceLanguage，要翻译的语言，参考开发文档 语言和发音人
     * @param {string} targetLanguage，翻译后的语言，参考开发文档 语言和发音人
     * @return {Promise.<string || null>} 成功返回翻译后的语言文本，失败返回null
     */
    async microphoneTranslationText(sourceLanguage, targetLanguage){
        let strData = this.setSendData("microphoneTranslationText", sourceLanguage, targetLanguage);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**音频文件翻译成目标语言文本
     * @param {string} audioPath， 要翻译的音频文件路径
     * @param {string} sourceLanguage，要翻译的语言，参考开发文档 语言和发音人
     * @param {string} targetLanguage，翻译后的语言，参考开发文档 语言和发音人
     * @return {Promise.<string || null>}成功返回翻译后的语言文本，失败返回null
     */
    async audioFileTranslationText(audioPath, sourceLanguage, targetLanguage){
        let strData = this.setSendData("audioFileTranslationText", audioPath, sourceLanguage, targetLanguage);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**初始化数字人，第一次初始化需要一些时间
     * @param {string} metahumanModePath, 数字人模型路径
     * @param {number} metahumanScaleWidth, 数字人宽度缩放倍数，1为原始大小。为2时放大一倍，0.5则缩小一半
     * @param {number} metahumanScaleHeight, 数字人高度缩放倍数，1为原始大小。为2时放大一倍，0.5则缩小一半
     * @param {boolean} isUpdateMetahuman, 是否强制更新，默认fasle。为true时强制更新会拖慢初始化速度
     * @param {boolean} enableRandomImage, 是否启用随机对比度、亮度和形变 等参数，默认fasle
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async initMetahuman(metahumanModePath, metahumanScaleWidth, metahumanScaleHeight, isUpdateMetahuman = false, enableRandomImage = false){
        let strData = this.setSendData("initMetahuman", metahumanModePath, metahumanScaleWidth, metahumanScaleHeight, isUpdateMetahuman, enableRandomImage);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**切换新的人物形象动作，此函数无需训练数字人模型，直接切换各种人物形象动作和场景。
     * @param {string} callApiKey, 调用函数的密钥
     * @param {string} actionVideoOrImage, 闭嘴的人物视频或者图片。素材要求：第一帧必须是正脸
     * @param {boolean} userSimValue, 是否使用近似值加速(影响精度)，默认不加速
     * @return {Promise.<boolean>} 成功返回true，失败返回false。调用不会立刻生效，加载完素材会自动切换。最后会生成以“素材文件大小”命名的".pt"后缀缓存文件，保证二次使用秒切换形象
     */
    async switchAction(callApiKey, actionVideoOrImage, userSimValue = false){
        let strData = this.setSendData("switchAction", callApiKey, actionVideoOrImage, userSimValue);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取切换人物形象动作状态
     * @return {Promise.<boolean>} false 表示正在切换，true 切换完成
     */
    async getSwitchActionState(){
        let strData = this.setSendData("getSwitchActionState");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**训练数字人，训练时长为10-30分钟
     * @param {string} callApiKey, 调用函数的密钥
     * @param {string} trainVideoOrImagePath, 闭嘴的人物视频或者图片 素材。素材要求：第一帧必须是正脸
     * @param {string} srcMetahumanModelPath, 预训练数字人模型路径
     * @param {string} saveHumanModelFolder, 保存训练完成的模型目录
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async trainHumanModel(callApiKey, trainVideoOrImagePath, srcMetahumanModelPath, saveHumanModelFolder){
        let strData = this.setSendData("trainHumanModel", callApiKey, trainVideoOrImagePath, srcMetahumanModelPath, saveHumanModelFolder);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**切换声音克隆模型
     * @param {string} cloneServerIp, 克隆声音服务端
     * @param {string} gptWeightsPath, gpt 模型权重路径。指克隆服务所在的电脑/服务器 路径
     * @param {string} sovitsWeightsPath, sovits 模型权重路径。指克隆服务所在的电脑/服务器 路径
     * @return {Promise.<boolean>} 失败返回false,成功返回true。 切换到与原模型无关音色的模型，切记更换参考音频和文本
     */
    async switchCloneAudioModel(cloneServerIp, gptWeightsPath, sovitsWeightsPath){
        let strData = this.setSendData("switchCloneAudioModel", cloneServerIp, gptWeightsPath, sovitsWeightsPath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**重启声音克隆服务
     * @param {string} cloneServerIp, 克隆声音服务端
     * @return {Promise.<boolean>} 失败返回false,成功返回true。重启服务会中断连接，实际并未准确返回值。重启后模型加载需要时间，调用此函数需显示等待几秒，再去访问声音克隆服务
     */
    async restartCloneAudioServer(cloneServerIp){
        let strData = this.setSendData("restartCloneAudioServer", cloneServerIp);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**克隆声音，需要部署服务端
     * @param {string} cloneServerIp, 克隆声音服务端
     * @param {string} saveAudioPath, 保存克隆声音的路径
     * @param {string} referAudioPath, 参考音频路径，3-10秒，音频时长不能大于等于10秒
     * @param {string} referText, 参考音频对应的文本
     * @param {string} cloneText, 要克隆的文本
     * @param {number} speedFactor, 语速（0.5为半速，1.0为正常速度，1.5为1.5倍速，以此类推）。默认为1.0 正常语速
     * @return {Promise.<boolean>} 失败返回false,成功返回true
     */
    async makeCloneAudio(cloneServerIp, saveAudioPath, referAudioPath, referText, cloneText, speedFactor = 1.0){
        let strData = this.setSendData("makeCloneAudio", cloneServerIp, saveAudioPath, referAudioPath, referText, cloneText, speedFactor);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**播报音频文件
     * @param {string} audioPath, 音频文件路径
     * @param {boolean} isWait, 是否等待.为true时,等待播放完毕
     * @return {Promise.<boolean>} 失败返回false,成功返回true
     */
    async playAudio(audioPath, isWait){
        let strData = this.setSendData("playAudio", audioPath, isWait);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**播报音频文件(EX)，playAudioEx 不能与 metahumanSpeechByFileEx 同步执行
     * @param {string} audioPath, 音频文件路径
     * @param {boolean} enableRandomParam, 是否启用随机去重参数
     * @param {boolean} isWait, 是否等待.为true时,等待播放完毕
     * @return {Promise.<boolean>} 总是返回true，函数仅添加播放音频文件到队列不处理返回
     */
    async playAudioEx(audioPath, enableRandomParam, isWait){
        let strData = this.setSendData("playAudioEx", audioPath, enableRandomParam, isWait);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**播报视频文件
     * @param {string} videoPath, 视频文件路径 (多个视频切换播放 视频和音频编码必须一致)
     * @param {number} videoSacle, 视频缩放（0.5缩小一半，1.0为原始大小）
     * @param {boolean} isLoopPlay, 是否循环播放
     * @param {boolean} enableRandomParam, 是否启用随机去重参数
     * @param {boolean} isWait, 是否等待播报完毕。 值为false时，不等待播放结束。未播报结束前再次调用此函数 会终止前面的播报内容。
     * @return {Promise.<boolean>} 失败返回false,成功返回true。
     */
    async playMedia(videoPath, videoSacle, isLoopPlay, enableRandomParam, isWait){
        let strData = this.setSendData("playMedia", videoPath,videoSacle, isLoopPlay, enableRandomParam, isWait);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**调节 playMedia 音量大小(底层用的内存共享，支持多进程控制)
     * @param {number} volumeScale, 音量缩放（0.5调低一半，1.0为原始音量大小）。默认为原始大小
     * @return {Promise.<boolean>} 失败返回false,成功返回true。
     */
    async setMediaVolumeScale(volumeScale = 1.0){
        let strData = this.setSendData("setMediaVolumeScale", volumeScale);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**生成lab文件，需要部署服务端
     * @param {string} labServerIp, lab服务端IP
     * @param {string} audioPath, 音频文件
     * @return {Promise.<boolean>} 失败返回false,成功返回true 并生成 与 audioPath 同目录下的 .lab 后缀文件。(音频文件+lab文件可以直接驱动数字人)
     */
    async makeCloneLab(labServerIp, audioPath){
        let strData = this.setSendData("makeCloneLab", labServerIp, audioPath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**语音识别，需要部署服务端
     * @param {string} labServerIp, lab服务端IP
     * @param {string} audioPath, 音频文件
     * @return {Promise.<boolean>} 失败返回null, 成功返回识别到的内容
     */
    async cloneAudioToText(labServerIp, audioPath){
        let strData = this.setSendData("cloneAudioToText", labServerIp, audioPath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**数字人说话，此函数需要调用 initSpeechService 初始化语音服务
     * @param {string} saveAudioPath, 保存的音频文件路径，扩展为.MP3格式。同名的 .lab文件需要和音频文件在同一目录下
     * @param {string} text,要转换语音的文本
     * @param {string} language，语言，参考开发文档 语言和发音人
     * @param {string} voiceName，发音人，参考开发文档 语言和发音人
     * @param {number} quality，音质，0低品质  1中品质  2高品质， 默认为0低品质
     * @param {boolean} waitPlaySound，等待音频播报完毕，默认为 true等待。为false时 多次调用此函数会添加到队列按顺序播报
     * @param {number} speechRate， 语速，默认为0，取值范围 -100 至 200
     * @param {string} voiceStyle，语音风格，默认General常规风格，其他风格参考开发文档 语言和发音人
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async metahumanSpeech(saveAudioPath, text, language, voiceName, quality = 0, waitPlaySound = true, speechRate = 0, voiceStyle = "General"){
        let strData = this.setSendData("metahumanSpeech", saveAudioPath, text, language, voiceName, quality, waitPlaySound, speechRate, voiceStyle);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**数字人说话内存缓存模式，需要调用 initSpeechService 初始化语音服务。函数一般用于常用的话术播报，非常用话术切勿使用，否则内存泄漏
     * @param {string} saveAudioPath, 保存的音频文件路径，扩展为.MP3格式。同名的 .lab文件需要和音频文件在同一目录下
     * @param {string} text,要转换语音的文本
     * @param {string} language，语言，参考开发文档 语言和发音人
     * @param {string} voiceName，发音人，参考开发文档 语言和发音人
     * @param {number} quality，音质，0低品质  1中品质  2高品质， 默认为0低品质
     * @param {boolean} waitPlaySound，等待音频播报完毕，默认为 true等待。为false时 多次调用此函数会添加到队列按顺序播报
     * @param {number} speechRate， 语速，默认为0，取值范围 -100 至 200
     * @param {string} voiceStyle，语音风格，默认General常规风格，其他风格参考开发文档 语言和发音人
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async metahumanSpeechCache(saveAudioPath, text, language, voiceName, quality = 0, waitPlaySound = true, speechRate = 0, voiceStyle = "General"){
        let strData = this.setSendData("metahumanSpeechCache", saveAudioPath, text, language, voiceName, quality, waitPlaySound, speechRate, voiceStyle);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**数字人说话文件缓存模式
     * @param {string} audioPath, 音频路径， 同名的 .lab文件需要和音频文件在同一目录下。若.lab文件不存在，则自动生成.lab文件。生成.lab文件产生的费用，请联系管理员
     * @param {boolean} waitPlaySound，等待音频播报完毕，默认为 true等待。为false时 多次调用此函数会添加到队列按顺序播报
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async metahumanSpeechByFile(audioPath, waitPlaySound = true){
        let strData = this.setSendData("metahumanSpeechByFile", audioPath, waitPlaySound);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**数字人说话文件缓存模式(Ex) metahumanSpeechByFileEx 不能与 PlayAudioEx 同步执行
     * @param {string} audioPath, 音频路径， 同名的 .lab文件需要和音频文件在同一目录下。若.lab文件不存在，则自动生成.lab文件。生成.lab文件产生的费用，请联系管理员
     * @param {boolean} enableRandomParam, 是否启用随机去重参数
     * @param {boolean} waitPlaySound，等待音频播报完毕，默认为 true等待。为false时 多次调用此函数会添加到队列按顺序播报
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async metahumanSpeechByFileEx(audioPath, enableRandomParam, waitPlaySound = true){
        let strData = this.setSendData("metahumanSpeechByFileEx", audioPath, enableRandomParam, waitPlaySound);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**打断数字人说话，一般用作人机对话场景。
     * metahumanSpeech和metahumanSpeechCache的 waitPlaySound 参数 设置为false时，此函数才有意义
     * @return {Promise.<boolean>} 返回true打断正在说话， 返回false 则为未说话状态
     */
    async metahumanSpeechBreak(){
        let strData = this.setSendData("metahumanSpeechBreak");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**数字人插入视频
     * @param {string} videoFilePath, 插入的视频文件路径
     * @param {string} audioFilePath, 插入的音频文件路径
     * @param {boolean} waitPlayVideo，等待视频播放完毕，默认为 true等待
     * @return {Promise.<boolean>} 总是返回true。 此函数依赖 initMetahuman函数运行，否则程序会崩溃
     */
    async metahumanInsertVideo(videoFilePath, audioFilePath, waitPlayVideo = true){
        let strData = this.setSendData("metahumanInsertVideo", videoFilePath, audioFilePath, waitPlayVideo);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**替换数字人背景
     * @param {string} bgFilePath,数字人背景 图片/视频 路径。仅替换绿幕背景的数字人模型
     * @param {number} replaceRed, 数字人背景的三通道之一的 R通道色值。默认-1 自动提取
     * @param {number} replaceGreen, 数字人背景的三通道之一的 G通道色值。默认-1 自动提取
     * @param {number} replaceBlue, 数字人背景的三通道之一的 B通道色值。默认-1 自动提取
     * @param {number} simValue, 相似度。 默认为0，此处参数用作微调RBG值。取值应当大于等于0
     * @return {Promise.<boolean>} 总是返回true。此函数依赖 initMetahuman函数运行，否则程序会崩溃
     */
    async replaceBackground(bgFilePath, replaceRed = -1, replaceGreen = -1, replaceBlue = -1, simValue = 0){
        let strData = this.setSendData("replaceBackground", bgFilePath, replaceRed, replaceGreen, replaceBlue, simValue);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**显示数字人说话的文本
     * @param {number} originY, 第一个字显示的起始Y坐标点。 默认0 自适应高度
     * @param {string} fontType, 字体样式，支持操作系统已安装的字体。例如"Arial"、"微软雅黑"、"楷体"
     * @param {number} fontSize, 字体的大小。默认30
     * @param {number} fontRed, 字体颜色三通道之一的 R通道色值。默认255
     * @param {number} fontGreen, 字体颜色三通道之一的 G通道色值。默认0
     * @param {number} fontBlue, 字体颜色三通道之一的 B通道色值。默认0
     * @param {boolean} italic, 是否斜体,默认false
     * @param {boolean} underline, 是否有下划线,默认false
     * @return {Promise.<boolean>} 总是返回true。此函数依赖 initMetahuman函数运行，否则程序会崩溃
     */
    async showSpeechText(originY = 0, fontType = "Arial", fontSize = 30, fontRed = 255, fontGreen = 0, fontBlue = 0, italic = false, underline = false){
        let strData = this.setSendData("showSpeechText", originY, fontType, fontSize, fontRed, fontGreen, fontBlue, italic, underline);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**生成数字人短视频，此函数需要调用 initSpeechService 初始化语音服务
     * @param {string} saveVideoFolder, 保存的视频目录
     * @param {string} text,要转换语音的文本
     * @param {string} language，语言，参考开发文档 语言和发音人
     * @param {string} voiceName，发音人，参考开发文档 语言和发音人
     * @param {string} bgFilePath,数字人背景 图片/视频 路径，扣除绿幕会自动获取绿幕的RGB值，null 则不替换背景。仅替换绿幕背景的数字人模型
     * @param {number} simValue, 相似度，默认为0。此处参数用作绿幕扣除微调RBG值。取值应当大于等于0
     * @param {string} voiceStyle，语音风格，默认General常规风格，其他风格参考开发文档 语言和发音人
     * @param {number} quality，音质，0低品质  1中品质  2高品质， 默认为0低品质
     * @param {number} speechRate， 语速，默认为0，取值范围 -100 至 200
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async makeMetahumanVideo(saveVideoFolder, text, language, voiceName, bgFilePath, simValue = 0, voiceStyle = "General", quality = 0,  speechRate = 0){
        let strData = this.setSendData("makeMetahumanVideo", saveVideoFolder, text, language, voiceName, bgFilePath, simValue, voiceStyle, quality,  speechRate);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**通过语音文件生成数字人短视频
     * @param {string} audioPath，音频路径， 同名的 .lab文件需要和音频文件在同一目录下
     * @param {string} bgFilePath,数字人背景 图片/视频 路径，扣除绿幕会自动获取绿幕的RGB值，null 则不替换背景。仅替换绿幕背景的数字人模型
     * @param {number} simValue, 相似度，默认为0。此处参数用作绿幕扣除微调RBG值。取值应当大于等于0
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async makeMetahumanVideoByFile(audioPath, bgFilePath, simValue = 0){
        let strData = this.setSendData("makeMetahumanVideoByFile", audioPath, bgFilePath, simValue);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**生成数字人说话文件，生成MP3文件和 lab文件，提供给 metahumanSpeechByFile 和使用
     * @param {string} saveAudioPath, 保存的音频文件路径，扩展为.MP3格式。同名的 .lab文件需要和音频文件在同一目录下
     * @param {string} text,要转换语音的文本
     * @param {string} language，语言，参考开发文档 语言和发音人
     * @param {string} voiceName，发音人，参考开发文档 语言和发音人
     * @param {number} quality，音质，0低品质  1中品质  2高品质， 默认为0低品质
     * @param {number} speechRate， 语速，默认为0，取值范围 -100 至 200
     * @param {string} voiceStyle，语音风格，默认General常规风格，其他风格参考开发文档 语言和发音人
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async makeMetahumanSpeechFile(saveAudioPath, text, language, voiceName, quality = 0, speechRate = 0, voiceStyle = "General"){
        let strData = this.setSendData("makeMetahumanSpeechFile", saveAudioPath, text, language, voiceName, quality, speechRate, voiceStyle);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    // /**初始化数字人声音克隆服务
    //  * @param {string} apiKey, API密钥
    //  * @param {string} voiceId, 声音ID
    //  * @return {Promise.<boolean>} 成功返回true，失败返回false
    // */
    // async initSpeechCloneService(apiKey, voiceId){
    //     let strData = this.setSendData("initSpeechCloneService", apiKey, voiceId);
    //     let byteRet = await this.sendData(strData);
    //     let strRet = byteRet.toString();
    //     if(strRet == "false")
    //         return false;
    //     else
    //         return true;
    // }

    // /**数字人使用克隆声音说话，此函数需要调用 initSpeechCloneService 初始化语音服务
    //  * @param {string} saveAudioPath, 保存的发音文件路径。这里是路径，不是目录！
    //  * @param {string} text,要转换语音的文本
    //  * @param {string} language，语言，中文：zh-cn，其他语言：other-languages
    //  * @param {boolean} waitPlaySound，等待音频播报完毕，默认为 true等待
    //  * @return {Promise.<boolean>} 成功返回true，失败返回false
    // */
    // async metahumanSpeechClone(saveAudioPath, text, language, waitPlaySound = true){
    //     let strData = this.setSendData("metahumanSpeechClone", saveAudioPath, text, language, waitPlaySound);
    //     let byteRet = await this.sendData(strData);
    //     let strRet = byteRet.toString();
    //     if(strRet == "false")
    //         return false;
    //     else
    //         return true;
    // }

    // /**使用克隆声音生成数字人短视频，此函数需要调用 initSpeechCloneService 初始化语音服务
    //  * @param {string} saveVideoFolder, 保存的视频和音频文件目录
    //  * @param {string} text,要转换语音的文本
    //  * @param {string} language，语言，中文：zh-cn，其他语言：other-languages
    //  * @param {string} bgFilePath,数字人背景 图片/视频 路径，扣除绿幕会自动获取绿幕的RGB值，null 则不替换背景。仅替换绿幕背景的数字人模型
    //  * @param {number} simValue, 相似度，默认为0。此处参数用作绿幕扣除微调RBG值。取值应当大于等于0
    //  * @return {Promise.<boolean>} 成功返回true，失败返回false
    // */
    // async makeMetahumanVideoClone(saveVideoFolder, text, language, bgFilePath, simValue = 0){
    //     let strData = this.setSendData("makeMetahumanVideoClone", saveVideoFolder, text, language, bgFilePath, simValue);
    //     let byteRet = await this.sendData(strData);
    //     let strRet = byteRet.toString();
    //     if(strRet == "false")
    //         return false;
    //     else
    //         return true;
    // }

    // /**生成数字人说话文件(声音克隆)，生成MP3文件和 lab文件，提供给 metahumanSpeechByFile 和使用
    //  * @param {string} saveAudioPath, 保存的发音文件路径。这里是路径，不是目录！
    //  * @param {string} text,要转换语音的文本
    //  * @param {string} language，语言，中文：zh-cn，其他语言：other-languages
    //  * @return {Promise.<boolean>} 成功返回true，失败返回false
    // */
    // async makeMetahumanSpeechFileClone(saveAudioPath, text, language){
    //     let strData = this.setSendData("makeMetahumanSpeechFileClone", saveAudioPath, text, language);
    //     let byteRet = await this.sendData(strData);
    //     let strRet = byteRet.toString();
    //     if(strRet == "false")
    //         return false;
    //     else
    //         return true;
    // }

    /**获取WindowsDriver.exe 命令扩展参数，一般用作脚本远程部署场景，WindowsDriver.exe驱动程序传递参数给脚本服务端
     * @return {Promise.<string>} 返回WindowsDriver驱动程序的命令行参数(不包含ip和port)
     */
    async getExtendParam(){
        let strData = this.setSendData("getExtendParam");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**获取Windows ID
     * @return {Promise.<string>} 成功返回Windows ID
     */
    async getWindowsId(){
        let strData = this.setSendData("getWindowsId");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**初始化Hid
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async initHid(){
        let strData = this.setSendData("initHid");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取Hid相关数据
     * @return {Promise.<[string] || void>} 成功返回激活成功的hid手机的安卓ID，失败返回null
     */
    async getHidData(){
        let strData = this.setSendData("getHidData");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "")
            return null;
        else
            return strRet.split("|");;
    }

    /**按下
     * @param {string} androidId, 安卓id
     * @param {number} angle, 手机旋转角度
     * @param {number} x, 横坐标
     * @param {number} y, 纵坐标
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidPress(androidId, angle, x, y){
        let strData = this.setSendData("hidPress", androidId, angle, x, y);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**移动
     * @param {string} androidId, 安卓id
     * @param {number} angle, 手机旋转角度
     * @param {number} x, 横坐标
     * @param {number} y, 纵坐标
     * @param {number} duration, 移动时长
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidMove(androidId, angle, x, y, duration){
        let strData = this.setSendData("hidMove", androidId, angle, x, y, duration);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**释放
     * @param {string} androidId, 安卓id
     * @param {number} angle, 手机旋转角度
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidRelease(androidId, angle){
        let strData = this.setSendData("hidRelease", androidId, angle);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**单击
     * @param {string} androidId, 安卓id
     * @param {number} angle, 手机旋转角度
     * @param {number} x, 横坐标
     * @param {number} y, 纵坐标
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidClick(androidId, angle, x, y){
        let strData = this.setSendData("hidClick", androidId, angle, x, y);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**双击
     * @param {string} androidId, 安卓id
     * @param {number} angle, 手机旋转角度
     * @param {number} x, 横坐标
     * @param {number} y, 纵坐标
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidDoubleClick(androidId, angle, x, y){
        let strData = this.setSendData("hidDoubleClick", androidId, angle, x, y);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**长按
     * @param {string} androidId, 安卓id
     * @param {number} angle, 手机旋转角度
     * @param {number} x, 横坐标
     * @param {number} y, 纵坐标
     * @param {number} duration, 按下时长
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidLongClick(androidId, angle, x, y, duration){
        let strData = this.setSendData("hidLongClick", androidId, angle, x, y, duration);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**滑动坐标
     * @param {string} androidId, 安卓id
     * @param {number} angle, 手机旋转角度
     * @param {number} startX, 起始横坐标
     * @param {number} startY, 起始纵坐标
     * @param {number} endX, 结束横坐标
     * @param {number} endY, 结束纵坐标
     * @param {number} duration, 滑动时长
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidSwipe(androidId, angle, startX, startY, endX, endY, duration){
        let strData = this.setSendData("hidSwipe", androidId, angle, startX, startY, endX, endY, duration);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**Hid手势
     * @param {string} androidId, 安卓id
     * @param {number} angle, 手机旋转角度
     * @param {[[x:number, y:number], [x1:number, y1:number]...]} gesturePath 手势路径
     * @param {number} duration 手势时长，单位毫秒
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidDispatchGesture(androidId, angle, gesturePath, duration){
        let strGesturePath = "";
        let arrLen = gesturePath.length;
        for(let i = 0; i < arrLen; i++){
            strGesturePath += gesturePath[i][0] + "/";
            strGesturePath += gesturePath[i][1] + "/";
            if(i < arrLen - 1)
                strGesturePath += "\n";
        }

        let strData = this.setSendData("hidDispatchGesture", androidId, angle, strGesturePath, duration);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**Hid多个手势
     * @param {string} androidId, 安卓id
     * @param {number} angle, 手机旋转角度
     * @param {[[duration:number, [x:number, y:number], [x1:number, y1:number]...],[duration:number, [x:number, y:number], [x1:number, y1:number]...],...]} gesturePaths  多点手势路径
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidDispatchGestures(androidId, angle, gesturePaths){
        let strGesturesPath = "";
        let arrLen1 = gesturePaths.length;
        for(let i = 0; i < arrLen1; i++){
            let arrLen2 = gesturePaths[i].length;
            strGesturesPath += gesturePaths[i][0] + "/";
            for(let j = 1; j < arrLen2; j++){
                strGesturesPath += gesturePaths[i][j][0] + "/";
                strGesturesPath += gesturePaths[i][j][1] + "/";
                if(j < arrLen2 - 1)
                    strGesturesPath += "\n";
            }
            if(i < arrLen1 - 1)
                strGesturesPath += "\r\n";
        }

        let strData = this.setSendData("hidDispatchGestures", androidId, angle, strGesturesPath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**hidBack
     * @param {string} androidId, 安卓id
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidBack(androidId){
        let strData = this.setSendData("hidBack", androidId);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**hidHome
     * @param {string} androidId, 安卓id
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidHome(androidId){
        let strData = this.setSendData("hidHome", androidId);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**hid显示最近任务
     * @param {string} androidId, 安卓id
     * @return {Promise.<boolean>} 成功返回true 失败返回false
     */
    async hidRecents(androidId){
        let strData = this.setSendData("hidRecents", androidId);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**导出原始音频，依赖 ffmpeg
     * @param {string} videoPath, 视频文件路径
     * @return {Promise.<boolean>} 失败返回false,成功返回true 并保存与视频同名的 .mp3 后缀文件
     */
    async extractAudio(videoPath){
        let strData = this.setSendData("extractAudio", videoPath);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**关闭WindowsDriver.exe驱动程序
     * @return {Promise.<void>}
     */
    async closeDriver(){
        let strData = this.setSendData("closeDriver");
        this.sendData(strData);
        return ;
    }

    /**激活框架
     * @param {string} activateKey, 激活密钥，联系管理员
     * @return {Promise.<string>} 返回激活信息
     */
    async activateFrame(activateKey){
        let strData = this.setSendData('activateFrame', activateKey);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**mcp服务
     * @param {string} apiKey, deepseek/智普AI密钥
     * @param {[]} message, 发送给大模型的信息
     * @param {[]} tools, 工具列表
     * @param {boolean} enableThinking 是否启用深度思考
     * @return {Promise.<JSON>} 成功返回json格式的内容，失败返回null
     */
    async mcp(apiKey, message, tools, enableThinking){
        const toolsString = JSON.stringify(tools, null, 4);
        const messageString = JSON.stringify(message, null, 4);
        let strData = this.setSendData('mcp', apiKey, messageString, toolsString, enableThinking);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "")
            return null;
        else
            return JSON.parse(strRet);
    }
}

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

module.exports = WindowsBot;