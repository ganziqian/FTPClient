package com.xjkb.ftpclient.hh;

import android.os.Message;
import android.util.Log;

import com.xjkb.ftpclient.Result;
import com.xjkb.ftpclient.Util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * FTP封装类.
 *
 * @author cui_tao
 */
public class FTP {
    /**
     * 服务器名.
     */
    private String hostName;

    /**
     * 用户名.
     */
    private String userName;

    /**
     * 密码.
     */
    private String password;

    /**
     * FTP连接.
     */
    private FTPClient ftpClient;

    /**
     * FTP列表.
     */
    private List<FTPFile> list;

    /**
     * FTP根目录.
     */
    public static final String REMOTE_PATH = "/home/ftp/ygsj/";

    /**
     * FTP当前目录.
     */
    private String currentPath = "/";

    /**
     * 统计流量.
     */
    private double response;

    private DownLoadProgressListener myListener;

    /**
     * 构造函数.
     * @param host hostName 服务器名
     * @param user userName 用户名
     * @param pass password 密码
     */
    public FTP(String host, String user, String pass) {
        this.hostName = host;
        this.userName = user;
        this.password = pass;
        this.ftpClient = new FTPClient();
        this.list = new ArrayList<FTPFile>();
    }

    /**
     * 打开FTP服务.
     * @throws IOException
     */
    public void openConnect() throws IOException {
        // 中文转码
        ftpClient.setControlEncoding("UTF-8");
        int reply; // 服务器响应值
        // 连接至服务器
        ftpClient.connect(hostName);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        }
        // 登录到服务器
        ftpClient.login(userName, password);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        } else {
            // 获取登录信息
            FTPClientConfig config = new FTPClientConfig(ftpClient.getSystemType().split(" ")[0]);
            config.setServerLanguageCode("zh");
            ftpClient.configure(config);
            // 使用被动模式设为默认
            ftpClient.enterLocalPassiveMode();
            // 二进制文件支持
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            System.out.println("login");
        }
    }

    /**
     * 关闭FTP服务.
     * @throws IOException
     */
    public void closeConnect() throws IOException {
        if (ftpClient != null) {
            // 登出FTP
            //  ftpClient.logout();
            // 断开连接
            ftpClient.disconnect();
            System.out.println("logout");
        }
    }

    /**
     * 列出FTP下所有文件.
     * @param remotePath 服务器目录
     * @return FTPFile集合
     * @throws IOException
     */
    public List<FTPFile> listFiles(String remotePath) throws IOException {
        // 获取文件
        FTPFile[] files = ftpClient.listFiles(remotePath);
        // 遍历并且添加到集合
        for (FTPFile file : files) {
            list.add(file);
        }
        return list;
    }

    /**
     * 下载.
     * @param remotePath FTP目录
     * @param fileName 文件名
     * @param localPath 本地目录
     * @return Result
     * @throws IOException
     */
    public Result download(String remotePath, String fileName, String localPath, DownLoadProgressListener myListener) throws IOException {
        boolean flag = true;
        Result result = null;
        this.myListener=myListener;
        // 初始化FTP当前目录
        currentPath = remotePath;
        // 初始化当前流量
        response = 0;
        // 更改FTP目录
        ftpClient.changeWorkingDirectory(remotePath);
        // 得到FTP当前目录下所有文件
        FTPFile[] ftpFiles = ftpClient.listFiles();
        // 循环遍历
        for (FTPFile ftpFile : ftpFiles) {
            Log.e("========",response+"");
            // 找到需要下载的文件
            if (ftpFile.getName().equals(fileName)) {
                System.out.println("download...");
                // 创建本地目录
                File file = new File(localPath + "/" + fileName);
                // 下载前时间
                Date startTime = new Date();
                if (ftpFile.isDirectory()) {
                    // 下载多个文件
                    flag = downloadMany(file);
                } else {
                    // 下载当个文件
                    flag = downloadSingle(file, ftpFile);
                }
                // 下载完时间
                Date endTime = new Date();
                // 返回值
                result = new Result(flag, Util.getFormatTime(endTime.getTime() - startTime.getTime()), Util.getFormatSize(response));
            }
        }
        return result;
    }
/*

    */
    /**
     * 下载单个文件.
     * @param localFile 本地目录
     * @param ftpFile FTP目录
     * @return true下载成功, false下载失败
     * @throws IOException
     */

    private boolean downloadSingle(File localFile, FTPFile ftpFile) throws IOException {
        boolean flag = true;

       /*
        // 创建输出流
        OutputStream outputStream = new FileOutputStream(localFile);
        // 统计流量
        response += ftpFile.getSize();
        // 下载单个文件

        Log.e("========",response+"");

        try {
            ftpClient.enterLocalPassiveMode();
            flag = ftpClient.retrieveFile(localFile.getName(), outputStream);
        }catch (NullPointerException e){
            e.printStackTrace();
            flag=false;
        }

        // 关闭文件流
        outputStream.close();
        return flag;*/



            Log.e("========>",currentPath);

            long step = ftpFile.getSize() / 100;
            long process = 0;
            long currentSize = 0;
            // 开始准备下载文件
            OutputStream out = new FileOutputStream(localFile, true);
            //   ftpClient.setRestartOffset(localSize);
            InputStream input = ftpClient.retrieveFileStream(new String(ftpFile.getName().getBytes("utf-8")));
            if (input != null) {
                Log.e("=======>>", input.toString());
                byte[] b = new byte[1024];
                int length = 0;
                while ((length = input.read(b)) != -1) {
                    out.write(b, 0, length);
                    currentSize = currentSize + length;
                    if (currentSize / step != process) {
                        process = currentSize / step;
                        if (process % 1 == 0) { // 每隔%5的进度返回一次
                            //进度
                            if (myListener != null) {
                             //   myListener.onDownLoadProgress("", process, null);
                            }
                        }
                    }
                }
            }
            out.flush();
            out.close();
            if (input != null) {
                input.close();
            }

            // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
            if (ftpClient.completePendingCommand()) {
                //成功
                flag = true;
            } else {
                //失败
                flag = false;
            }


            Log.e("ftp-ont", flag + "");
       // }
        return flag;
    }


    /*
        * 下载进度监听
        */
    public interface DownLoadProgressListener {
        public void onDownLoadProgress(String currentStep, long downProcess, File file);
    }



















    /**
     * 下载服务器上的文件或者文件夹
     * @param ftpFileName 服务器上的文件夹名或者文件名
     * @param localDir 下载到设备上的路径
     */
    public int downFileOrDir(String ftpFileName, String localDir, int downloadFileCount) {
        Message downloadFileMsg;
        // downloadFileMsg = mHandler.obtainMessage();
        int downloadCount = 0;
        OutputStream fos;
        try {

            File file = new File(ftpFileName);
            File temp = new File(localDir);
            if (!temp.exists()) {
                temp.mkdirs();
            }
            // 判断是否是目录
            if (isDir(ftpFileName)) {
                String[] names = ftpClient.listNames();
                for (int i = 0; i < names.length; i++) {
                    if (isDir(names[i])) {
                        downFileOrDir(ftpFileName + '/' + names[i], localDir
                                + File.separator + names[i], downloadFileCount);
                        ftpClient.changeToParentDirectory();
                    } else {

                        File localfile = new File(localDir + File.separator
                                + names[i]);
                        if (!localfile.exists()) {
                            fos = new FileOutputStream(localfile);
                            ftpClient.retrieveFile(names[i], fos);
                        } else {
                            // 开始删除文件
                            file.delete();
                            fos = new FileOutputStream(localfile);
                            ftpClient.retrieveFile(ftpFileName, fos);
                        }
                    }
                }
            } else {
                downloadCount = downloadFileCount + 1;
                File localfile = new File(localDir + File.separator
                        + file.getName());
                if (!localfile.exists()) {
                    fos = new FileOutputStream(localfile);
                    ftpClient.retrieveFile(ftpFileName, fos);
                } else {
                    // 开始删除文件
                    file.delete();
                    fos = new FileOutputStream(localfile);
                    ftpClient.retrieveFile(ftpFileName, fos);
                }
                ftpClient.changeToParentDirectory();

            }
        } catch (SocketException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }
        Log.e("===========","-------------------");
        return downloadCount;
    }

    // 判断是否是目录
    public boolean isDir(String fileName) {
        try {
            // 切换目录，若当前是目录则返回true,否则返回true。
            boolean falg = ftpClient.changeWorkingDirectory(fileName);
            return falg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }



    /**
     * 下载多个文件.
     * @param localFile 本地目录
     * @return true下载成功, false下载失败
     * @throws IOException
     */
    private boolean downloadMany(File localFile) throws IOException {
        boolean flag = true;
        // FTP当前目录
        if (!currentPath.equals(REMOTE_PATH)) {
            currentPath = currentPath + REMOTE_PATH + localFile.getName();
        } else {
            currentPath = currentPath + localFile.getName();
        }
        // 创建本地文件夹
        localFile.mkdir();
        // 更改FTP当前目录
        ftpClient.changeWorkingDirectory(currentPath);
        // 得到FTP当前目录下所有文件
        FTPFile[] ftpFiles = ftpClient.listFiles();
        // 循环遍历
        for (FTPFile ftpFile : ftpFiles) {
            Log.e("========",response+"");
            // 创建文件
            File file = new File(localFile.getPath() + "/" + ftpFile.getName());
            if (ftpFile.isDirectory()) {
                // 下载多个文件
                flag = downloadMany(file);
            } else {
                // 下载单个文件
                flag = downloadSingle(file, ftpFile);
            }
        }
        Log.e("ftp-man",flag+"");
        return flag;
    }

    /**
     * 上传.
     * @param localFile 本地文件
     * @param remotePath FTP目录
     * @return Result
     * @throws IOException
     */
    public Result uploading(File localFile, String remotePath) throws IOException {
        boolean flag = true;
        Result result = null;
        // 初始化FTP当前目录
        currentPath = remotePath;
        // 初始化当前流量
        response = 0;
        // 二进制文件支持
        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        // 使用被动模式设为默认
        ftpClient.enterLocalPassiveMode();
        // 设置模式
        ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
        // 改变FTP目录
        ftpClient.changeWorkingDirectory(REMOTE_PATH);
        // 获取上传前时间
        Date startTime = new Date();
        if (localFile.isDirectory()) {
            // 上传多个文件
            flag = uploadingMany(localFile);
        } else {
            // 上传单个文件
            flag = uploadingSingle(localFile);
        }
        // 获取上传后时间
        Date endTime = new Date();
        // 返回值
        result = new Result(flag, Util.getFormatTime(endTime.getTime() - startTime.getTime()), Util.getFormatSize(response));
        return result;
    }

    /**
     * 上传单个文件.
     * @param localFile 本地文件
     * @return true上传成功, false上传失败
     * @throws IOException
     */
    private boolean uploadingSingle(File localFile) throws IOException {
        boolean flag = true;
        // 创建输入流
        InputStream inputStream = new FileInputStream(localFile);
        // 统计流量
        response += (double) inputStream.available() / 1;
        // 上传单个文件
        flag = ftpClient.storeFile(localFile.getName(), inputStream);
        // 关闭文件流
        inputStream.close();
        return flag;
    }

    /**
     * 上传多个文件.
     * @param localFile 本地文件夹
     * @return true上传成功, false上传失败
     * @throws IOException
     */
    private boolean uploadingMany(File localFile) throws IOException {
        boolean flag = true;
        // FTP当前目录
        if (!currentPath.equals(REMOTE_PATH)) {
            currentPath = currentPath + REMOTE_PATH + localFile.getName();
        } else {
            currentPath = currentPath + localFile.getName();
        }
        // FTP下创建文件夹
        ftpClient.makeDirectory(currentPath);
        // 更改FTP目录
        ftpClient.changeWorkingDirectory(currentPath);
        // 得到当前目录下所有文件
        File[] files = localFile.listFiles();
        // 遍历得到每个文件并上传
        for (File file : files) {
            if (file.isHidden()) {
                continue;
            }
            if (file.isDirectory()) {
                // 上传多个文件
                flag = uploadingMany(file);
            } else {
                // 上传单个文件
                flag = uploadingSingle(file);
            }
        }
        return flag;
    }










/*

    */
/**
     2     * 解压缩功能.
     3     * 将zipFile文件解压到folderPath目录下.
     4     * @throws Exception
     5 *//*

    public int upZipFile(File zipFile, String folderPath)throws ZipException,IOException {
        //public static void upZipFile() throws Exception{
        ZipFile zfile=new ZipFile(zipFile);
        Enumeration zList=zfile.entries();
        ZipEntry ze=null;
        byte[] buf=new byte[1024];
        while(zList.hasMoreElements()){
            ze=(ZipEntry)zList.nextElement();
            if(ze.isDirectory()){
                Log.d("upZipFile", "ze.getName() = "+ze.getName());
                String dirstr = folderPath + ze.getName();
                //dirstr.trim();
                dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
                Log.d("upZipFile", "str = "+dirstr);
                File f=new File(dirstr);
                f.mkdir();
                continue;
            }
            Log.d("upZipFile", "ze.getName() = "+ze.getName());
            OutputStream os=new BufferedOutputStream(new FileOutputStream(getRealFileName(folderPath, ze.getName())));
            InputStream is=new BufferedInputStream(zfile.getInputStream(ze));
            int readLen=0;
            while ((readLen=is.read(buf, 0, 1024))!=-1) {
                os.write(buf, 0, readLen);
            }
            is.close();
            os.close();
        }
        zfile.close();
        Log.d("upZipFile", "finishssssssssssssssssssss");
        return 0;
    }
*/


}
