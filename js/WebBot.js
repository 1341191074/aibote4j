const fs = require('fs');
const net = require('net');
const path = require("path");
const child_process = require('child_process');

class WebBot{
    static server = [];
    /**
     * @param {function(WebBot)} webMain 要注册的函数，必须含一个参数，用作接收WebBot对象
     * @param {number} ip 脚本所在的地址，传递给WebDriver.exe。如果值为 "127.0.0.1"脚本会将参数 ip和port作为启动参数并启动WebDriver.exe，否则用户需要手动启动WebDriver.exe 并且提供启动参数。
     * @param {number} port 监听端口，传递给WebDriver.exe
     * @param {{browserName:string, debugPort:number, userDataDir:string, browserPath:string, argument:string, extendParam:string}} options 可选参数
     * browserName 浏览器名称，默认 chrome 浏览器。除edge和chrome浏览器会自动寻找浏览器路径，其他浏览器需要指定browserPath。
     * debugPort 调试端口,默认 0 随机端口。指定端口则接管已打开的浏览器。启动浏览应指定的参数 --remote-debugging-port=19222 --user-data-dir=C:\\Users\\电脑用户名\\AppData\\Local\\Google\\Chrome\\User Data
     * userDataDir 用户数据目录。多进程同时操作多个浏览器数据目录不能相同
     * browserPath 浏览器路径
     * argument 浏览器启动参数。例如：设置代理：--proxy-server=127.0.0.1:8080   无头模式: --headless  浏览器版本>112 的无头模式:--headless=new
     * extendParam 扩展参数，一般用作脚本远程部署场景，WebDriver.exe驱动程序传递参数给脚本服务端
     * @param {string} driverFolder 驱动程序所在的文件夹，默认 "../"
     */
    static registerMain(webMain, ip, port, options = {}, driverFolder = "../"){
        let browserName = "chrome";
        if(options["browserName"] != undefined)
            browserName = options["browserName"];

        let debugPort = 0;
        if(options["debugPort"] != undefined)
            debugPort = options["debugPort"];

        let userDataDir = "null";
        if(options["userDataDir"] != undefined)
            userDataDir = options["userDataDir"];

        let browserPath = "null";
        if(options["browserPath"] != undefined)
            browserPath = options["browserPath"];

        let argument = "null";
        if(options["argument"] != undefined)
            argument = options["argument"];

        let extendParam = "";
        if(options["extendParam"] != undefined)
            extendParam = options["extendParam"];

        let browserParam = `{"serverIp":"${ip}", "serverPort":${port}, "browserName":"${browserName}", "debugPort":${debugPort}, "userDataDir":"${userDataDir}",
                                                                            "browserPath":"${browserPath}", "argument":"${argument}", "extendParam":"${extendParam}"}`;
        browserParam = browserParam.replace(/\\/g, "\\\\");//c++ json “\” 需要转义
        if(ip == "127.0.0.1"){
            //获取驱动程序路径
            let driverPath = driverFolder + "WebDriver.exe";//path.resolve(__dirname, "../../WebDriver.exe");
            let isExist = fs.existsSync(driverPath);
            if(!isExist)
                driverPath = 'WebDriver.exe';

            child_process.execFile(driverPath, [browserParam]);
            console.log("正在启动WebDriver...");
        }

        let serverIndex = WebBot.server.length;
        WebBot.server[serverIndex] = new net.createServer();
        WebBot.server[serverIndex].listen(port);
        WebBot.server[serverIndex].on('connection', (clientSocket) => {
            webMain(new WebBot(clientSocket));
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

        this.socket.on('error', error=>{
            console.log(error);
        });

        this.socket.on('close', ()=>{
            console.log('WebBot已关闭');
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
     * @param {number} intervalMs 心跳间隔，单位毫秒。可选参数，默认100毫秒
     * @return {Promise.<void>}
     */
    async setImplicitTimeout(waitMs, intervalMs = 100){
        this.waitTimeout = waitMs;
        this.intervalTimeout = intervalMs;
    }

    /**跳转url
     * @param {string} url 要跳转的链接，必须http://或https:// 起头
     * @return {Promise.<boolean>} 总是返回true
     */
    async goto(url){
        let strData = this.setSendData("goto", url);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**新建tab页面并跳转到指定url
     * @param {string} url 跳转的链接，必须http://或https:// 起头
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async newPage(url){
        let strData = this.setSendData("newPage", url);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**返回
     * @return {Promise.<boolean>} 总是返回true
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

    /**前进
     * @return {Promise.<boolean>} 总是返回true
     */
    async forward(){
        let strData = this.setSendData("forward");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**刷新
     * @return {Promise.<boolean>} 总是返回true
     */
    async refresh(){
        let strData = this.setSendData("refresh");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取当前页面ID
     * @return {Promise.<string>} 成功返回页面ID
     */
    async getCurPageId(){
        let strData = this.setSendData("getCurPageId");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**获取所有页面ID
     * @return {Promise.<[string]>} 成功返回页面ID数组,失败返回null
     */
    async getAllPageId(){
        let strData = this.setSendData("getAllPageId");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet.split("|");
    }

    /**切换指定页面
     * @param {string} pageId 要切换的页面ID
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async switchPage(pageId){
        let strData = this.setSendData("switchPage", pageId);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**关闭当前页面
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async closePage(){
        let strData = this.setSendData("closePage");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取当前页面URL
     * @return {Promise.<string>}
     */
    async getCurrentUrl(){
        let strData = this.setSendData("getCurrentUrl");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**获取当前页面标题
     * @return {Promise.<string>}
     */
    async getTitle(){
        let strData = this.setSendData("getTitle");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**切换frame
     * @param {string} xpath 要切换frame的元素路径
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async switchFrame(xpath){
        let strData = this.setSendData("switchFrame", xpath);
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

    /**切换到主frame
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async switchMainFrame(){
        let strData = this.setSendData("switchMainFrame");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**点击元素
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async clickElement(xpath){
        let strData = this.setSendData("clickElement", xpath);
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

    /**设置编辑框值
     * @param {string} xpath 元素路径
     * @param {string} value 输入的值
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async setElementValue(xpath, value){
        let strData = this.setSendData("setElementValue", xpath, value);
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

    /**获取编辑框值
     * @param {string} xpath 元素路径
     * @return {Promise.<string>} 成功返回编辑框值，失败返回null
     */
    async getElementValue(xpath){
        let command = `(function () {\
            let element = document.evaluate('${xpath}', document).iterateNext();\
            if(element == null)\
                return null;\
            else\
                return element.value;\
        })()`;

        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let value;
        do{
            value = await this.executeScript(command);
            if(value == null)
                await this.sleep(this.intervalTimeout);
            else
                break;
            endTime = process.uptime() * 1000;
        }while(endTime - startTime <= this.waitTimeout);

        return value;
    }

    /**获取文本
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回元素文本，失败返回null
     */
    async getElementText(xpath){
        let strData = this.setSendData("getElementText", xpath);
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

    /**获取上下文
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回元素文本，失败返回null
     */
    async getElementContent(xpath){
        let strData = this.setSendData("getElementContent", xpath);
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

    /**获取outerHTML
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回元素outerHTML，失败返回null
     */
    async getElementOuterHTML(xpath){
        let strData = this.setSendData("getElementOuterHTML", xpath);
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

    /**获取innerHTML
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回元素innerHTML，失败返回null
     */
    async getElementInnerHTML(xpath){
        let strData = this.setSendData("getElementInnerHTML", xpath);
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

    /**设置属性值
     * @param {string} xpath 元素路径
     * @param {string} name 属性名
     * @param {string} value 属性值
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async setElementAttribute(xpath, name, value){
        let strData = this.setSendData("setElementAttribute", xpath, name, value);
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

    /**获取指定属性的值
     * @param {string} xpath 元素路径
     * @param {string} name 属性名
     * @return {Promise.<boolean>} 成功返回元素属性值，失败返回null
     */
    async getElementAttribute(xpath, name){
        let strData = this.setSendData("getElementAttribute", xpath, name);
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

    /**获取矩形位置
     * @param {string} xpath 元素路径
     * @return {Promise.<{left:number, top:number, right:number, bottom:number, width:number, height:number}>} 成功返回矩形位置，失败返回null
     */
    async getElementRect(xpath){
        let strData = this.setSendData("getElementRect", xpath);
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
            return JSON.parse(strRet);
    }

    /**判断元素是否选中
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 选中返回true，否则返回false
     */
    async isSelected(xpath){
        let strData = this.setSendData("isSelected", xpath);
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let strRet, byteRet;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "webdriver error")
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

    /**判断元素是否可见
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 选中返回true，否则返回false
     */
    async isDisplayed(xpath){
        let strData = this.setSendData("isDisplayed", xpath);
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let strRet, byteRet;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "webdriver error")
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

    /**判断元素是否可用
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 选中返回true，否则返回false
     */
    async isEnabled(xpath){
        let strData = this.setSendData("isEnabled", xpath);
        let startTime = process.uptime() * 1000;
        let endTime = process.uptime() * 1000;
        let strRet, byteRet;
        do{
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
            if(strRet == "webdriver error")
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

    /**清空元素值
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async clearElement(xpath){
        let strData = this.setSendData("clearElement", xpath);
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

    /**设置元素焦点
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async setElementFocus(xpath){
        let strData = this.setSendData("setElementFocus", xpath);
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

    /**通过元素上传文件
     * @param {string} xpath 元素路径，上传文件路径一般含有 <input type="file" >标签
     * @param {string} filePath 本地文件路径，路径不存在会导致网页崩溃
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async uploadFile(xpath, filePath){
        let strData = this.setSendData("uploadFile", xpath, filePath);
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

    /**显示元素xpath路径，页面加载完毕再调用。
     * 调用此函数后，可在页面移动鼠标会显示元素区域。移动并按下ctrl键，会显示 相对/绝对/文本 xpath路径
     * ifrmae 内的元素，需要调用 switchFrame 切入进去
     * @return {Promise.<boolean>} 总是返回true
     */
    async showXpath(){
        let strData = this.setSendData("showXpath");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**获取可见区域内的所有元素信息
     * @return {Promise.<{elements:[{}]}|null>} 成功返回json格式的元素信息，失败返回null
     */
    async getElements(){
        let strData = this.setSendData("getElements");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        try{
            return JSON.parse(strRet);
        }catch(e){
            return null;
        }
    }

    /**输入文本
     * @param {string} xpath 元素路径，如果元素不能设置焦点，应ClickMouse 点击锁定焦点输入
     * @param {string} text 要输入的文本，例如sendKeys('//*[@id="kw"]', 'aibote\r'); aibote换行
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async sendKeys(xpath, text){
        let strData = this.setSendData("sendKeys", xpath, text);
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

    /**发送Vk虚拟键
     * @param {number} vkCode VK键值，仅支持 回退键:8  制表键:9  回车键:13  空格键:32  方向左键:37  方向上键:38  方向右键:39  方向下键:40  删除键:46
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async sendVk(vkCode){
        let strData = this.setSendData("sendVk", vkCode);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**点击鼠标
     * @param {number} x 横坐标，非Windows坐标，页面左上角为起始坐标
     * @param {number} y 纵坐标，非Windows坐标，页面左上角为起始坐标
     * @param {number} msg 单击左键:1  单击右键:2  按下左键:3  弹起左键:4  按下右键:5  弹起右键:6  双击左键：7
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async clickMouse(x, y, msg){
        let strData = this.setSendData("clickMouse", x, y, msg);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**移动鼠标
     * @param {number} x 同clickMouse解释一致
     * @param {number} y 同clickMouse解释一致
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async moveMouse(x, y){
        let strData = this.setSendData("moveMouse", x, y);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**滚动鼠标
     * @param {number} deltaX 水平滚动条移动的距离，正数向右滚动，负数向左滚动
     * @param {number} deltaY 垂直滚动条移动的距离，正数向下滚动，负数向上滚动
     * @param {number} x 可选参数，鼠标横坐标位置， 默认在页面窗口中间
     * @param {number} y 可选参数，鼠标纵坐标位置， 默认在页面窗口中间
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async wheelMouse(deltaX, deltaY, x = 0, y = 0){
        if(x == 0 && y == 0){
            const windowPos = await this.getWindowPos();
            x = windowPos.width / 2;
            y = windowPos.height / 2;
        }

        let strData = this.setSendData("wheelMouse", deltaX, deltaY, x, y);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**通过xpath 点击鼠标
     * @param {string} xpath 元素路径
     * @param {number} msg 单击左键:1  单击右键:2  按下左键:3  弹起左键:4  按下右键:5  弹起右键:6  双击左键：7
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async clickMouseByXpath(xpath, msg){
        let strData = this.setSendData("clickMouseByXpath", xpath, msg);
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

    /**通过xpath 移动鼠标到元素的中心点
     * @param {string} xpath 元素路径
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async moveMouseByXpath(xpath){
        let strData = this.setSendData("moveMouseByXpath", xpath);
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

    /**通过xpath 滚动鼠标
     * @param {string} xpath 元素路径
     * @param {number} deltaX 水平滚动条移动的距离
     * @param {number} deltaY 垂直滚动条移动的距离
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async wheelMouseByXpath(xpath, deltaX, deltaY){
        let strData = this.setSendData("wheelMouseByXpath", xpath, deltaX, deltaY);
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

    /**仿真模式 开始触屏
     * @param {number} x 非Windows坐标，页面左上角为起始坐标，可以通过getElementRect 获取相关坐标
     * @param {number} y 非Windows坐标，页面左上角为起始坐标，可以通过getElementRect 获取相关坐标
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async touchStart(x, y){
        let strData = this.setSendData("touchStart", x, y);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**仿真模式 移动触屏
     * @param {number} x 同touchStart
     * @param {number} y 同touchStart
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async touchMove(x, y){
        let strData = this.setSendData("touchMove", x, y);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**仿真模式 结束触屏
     * @param {number} x 一般同最后一个触屏事件的坐标一致
     * @param {number} y 一般同最后一个触屏事件的坐标一致
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async touchEnd(x, y){
        let strData = this.setSendData("touchEnd", x, y);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**截图
     * @param {string} xpath 可选参数，元素路径。如果指定该参数则截取元素图片
     * @return {Promise.<string|boolean>} 成功返回PNG图片格式 base64 字符串，失败返回null
     */
    async takeScreenshot(xpath = null){
        let strData, strRet, byteRet;
        if(xpath == null){
            strData = this.setSendData("takeScreenshot");
            byteRet = await this.sendData(strData);
            strRet = byteRet.toString();
        }
        else{
            strData = this.setSendData("takeScreenshot", xpath);
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
        }

        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**点击警告框
     * @param {boolean} acceptOrCancel true接受, false取消
     * @param {string} promptText 可选参数，输入prompt警告框文本
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async clickAlert(acceptOrCancel, promptText = ""){
        let strData = this.setSendData("clickAlert", acceptOrCancel, promptText);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取警告框内容
     * @return {Promise.<boolean>} 返回警告框内容
     */
    async getAlertText(){
        let strData = this.setSendData("getAlertText");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**获取指定url匹配的cookies
     * @param {string} url 指定的url http://或https:// 起头
     * @return {Promise.<string>} 成功返回json格式的字符串，失败返回null
     */
    async getCookies(url){
        let strData = this.setSendData("getCookies", url);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**获取所有cookies
     * @return {Promise.<string>} 成功返回json格式的字符串，失败返回null
     */
    async getAllCookies(){
        let strData = this.setSendData("getAllCookies");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**设置cookie
     * @param {{"name":string, "value":string, "url":string, "domain":string, "path":string, "secure":boolean, "httpOnly":boolean, "sameSite":string,
     * "expires":number, "priority":string, "sameParty":boolean, "sourceScheme":string, "sourcePort":number, "partitionKey":string}} cookieParam
     * cookie参数， name、value和url必填参数，其他参数可选
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async setCookie(cookieParam){
        let name = cookieParam["name"];
        let value = cookieParam["value"];
        let url = cookieParam["url"];
        let domain = "", path = "", secure = false, httpOnly = false, sameSite = "", expires = 0, priority = "", sameParty = false, sourceScheme = "", sourcePort = 0, partitionKey = "";
        if(cookieParam["domain"] != undefined)
            domain = cookieParam["domain"];
        if(cookieParam["path"] != undefined)
            path = cookieParam["path"];
        if(cookieParam["secure"] != undefined)
            secure = cookieParam["secure"];
        if(cookieParam["httpOnly"] != undefined)
            httpOnly = cookieParam["httpOnly"];
        if(cookieParam["sameSite"] != undefined)
            sameSite = cookieParam["sameSite"];
        if(cookieParam["expires"] != undefined)
            expires = cookieParam["expires"];
        if(cookieParam["priority"] != undefined)
            priority = cookieParam["priority"];
        if(cookieParam["sameParty"] != undefined)
            sameParty = cookieParam["sameParty"];
        if(cookieParam["sourceScheme"] != undefined)
            sourceScheme = cookieParam["sourceScheme"];
        if(cookieParam["sourcePort"] != undefined)
            sourcePort = cookieParam["sourcePort"];
        if(cookieParam["partitionKey"] != undefined)
            partitionKey = cookieParam["partitionKey"];

        let strData = this.setSendData("setCookie", name, value, url, domain, path, secure, httpOnly, sameSite, expires, priority, sameParty, sourceScheme, sourcePort, partitionKey);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**删除指定cookies
     * @param {string} name 要删除的 Cookie 的名称。
     * @param {{url:string, domain:string, path:string}} options 可选参数
     * url 如果指定，则删除所有匹配 url 和 name的Cookie
     * domain 如果指定，则删除所有匹配 domain 和 name的Cookie
     * path 如果指定，则删除所有匹配 path 和 name的Cookie
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async deleteCookies(name, options = {}){
        let url = "";
        if(options["url"] != undefined)
            url = options["url"];

        let domain = "";
        if(options["domain"] != undefined)
            domain = options["domain"];

        let path = "";
        if(options["path"] != undefined)
            path = options["path"];

        let strData = this.setSendData("deleteCookies", name, url, domain, path);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**删除所有cookies
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async deleteAllCookies(){
        let strData = this.setSendData("deleteAllCookies");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**清除缓存
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async clearCache(){
        let strData = this.setSendData("clearCache");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**注入JavaScript
     * @param {string} command 注入的js代码
     * @return {Promise.<string>} 代码字符串。若获取注入js执行结果，必须通过函数的形式返回，例如：(function () {return '返回值'})(); 目前返回值仅支持字符串，如执行结果是对象类型请先转换字符串再返回。;
     */
    async executeScript(command){
        let strData = this.setSendData("executeScript", command);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return strRet;
    }

    /**获取窗口位置和状态
     * @return {Promise.<{left:number, top:number, width:number, height:number, windowState:string}>} 成功返回矩形位置和窗口状态，失败返回null
     */
    async getWindowPos(){
        let strData = this.setSendData("getWindowPos");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "null")
            return null;
        else
            return JSON.parse(strRet);
    }

    /**设置窗口位置和状态
     * @param {string} windowState 窗口状态，正常:"normal"  最小化:"minimized"  最大化:"maximized"  全屏:"fullscreen"
     * @param {{left:number, top:number, width:number, height:number}} 可选参数，浏览器窗口位置，此参数仅windowState 值为 "normal" 时有效
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async setWindowPos(windowState, rect = {left:0, top:0, width:0, height:0}){
        let strData = this.setSendData("setWindowPos", windowState, rect["left"], rect["top"], rect["width"], rect["height"]);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**获取WebDriver.exe 命令扩展参数，一般用作脚本远程部署场景，WebDriver.exe驱动程序传递参数给脚本服务端
     * @return {Promise.<string>} 返回WebDriver 驱动程序的命令行["extendParam"] 字段的参数
     */
    async getExtendParam(){
        let strData = this.setSendData("getExtendParam");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        return strRet;
    }

    /**手机浏览器仿真
     * @param {number} width 宽度
     * @param {number} height 高度
     * @param {string} userAgent 用户代理
     * @param {string} platform 系统平台，例如 "Android"、"IOS"、"iPhone"
     * @param {string} platformVersion 系统版本号，例如 "9.0"，应当与userAgent提供的版本号对应
     * @param {string} acceptLanguage 可选参数，语言，例如 "zh-CN"、"en"
     * @param {string} timezoneId 可选参数，时区标识，例如"Asia/Shanghai"、"Europe/Berlin"、"Europe/London" 时区应当与 语言、经纬度 对应
     * @param {number} latitude 可选参数，纬度，例如 31.230416
     * @param {number} longitude 可选参数，经度，例如 121.473701
     * @param {number} accuracy 可选参数，精度，例如 1111
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async mobileEmulation(width, height, userAgent, platform, platformVersion, acceptLanguage = "", timezoneId = "", latitude = 0, longitude = 0, accuracy = 0){
        let strData = this.setSendData("mobileEmulation", width, height, userAgent, platform, platformVersion, acceptLanguage, timezoneId, latitude, longitude, accuracy);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**设置浏览器下载目录
     * @param {string} downloadDir 存放下载的目录
     * @return {Promise.<boolean>} 成功返回true，失败返回false
     */
    async setDownloadDir(downloadDir){
        let strData = this.setSendData("setDownloadDir", downloadDir);
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**关闭浏览器
     * @return {Promise.<boolean>} 总是返回true
     */
    async closeBrowser(){
        let strData = this.setSendData("closeBrowser");
        let byteRet = await this.sendData(strData);
        let strRet = byteRet.toString();
        if(strRet == "false")
            return false;
        else
            return true;
    }

    /**关闭WebDriver.exe驱动程序
     * @return {Promise.<void>}
     */
    async closeDriver(){
        let strData = this.setSendData('closeDriver');
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

module.exports = WebBot;