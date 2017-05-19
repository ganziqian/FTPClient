package com.xjkb.ftpclient.fore;

import android.util.Log;

import com.xjkb.ftpclient.Result;
import com.xjkb.ftpclient.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public static final String REMOTE_PATH = "/";

    /**
     * FTP当前目录.
     */
    private String currentPath = "/";

    /**
     * 统计流量.
     */
    private double response;

    private List<String> ftpPaths=new ArrayList<>();

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
        ftpClient.setControlEncoding("GBK");
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
            ftpClient.logout();
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
    private DownNameLinten downNameLinten;
    /**
     * 下载.
     * @param remotePath FTP目录
     * @param fileName 文件名
     * @param localPath 本地目录
     * @return Result
     * @throws IOException
     */
    public Result download(String remotePath, String fileName, String localPath, DownNameLinten downNameLinten2) throws IOException {
        boolean flag = true;
        Result result = null;
        // 初始化FTP当前目录
        currentPath = remotePath;
        downNameLinten=downNameLinten2;
        // 初始化当前流量
        response = 0;
        // 更改FTP目录
        ftpClient.changeWorkingDirectory(remotePath);
        // 得到FTP当前目录下所有文件
        FTPFile[] ftpFiles = ftpClient.listFiles();


        Date startTime = new Date();
        // 循环遍历
        for (FTPFile ftpFile : ftpFiles) {


            if(fileName.equals("")) {
                // 找到需要下载的文件
                // 下载前时间
                if (ftpFile.isDirectory()) {
                    // 下载多个文件
                    flag = downloadMany(REMOTE_PATH + ftpFile.getName());
                } else {
                    // 下载当个文件
                    flag = ftpPaths.add(REMOTE_PATH + ftpFile.getName());
                }
            }else{
                if(fileName.equals(ftpFile.getName())) {
                    if (ftpFile.isDirectory()) {
                        // 下载多个文件
                        flag = downloadMany(REMOTE_PATH + ftpFile.getName());
                    } else {
                        // 下载当个文件
                        flag = ftpPaths.add(REMOTE_PATH + ftpFile.getName());
                    }
                    break;
                }

            }



            // 下载完时间

            // 返回值

        }




        Log.e("----->>>",ftpPaths.size()+"");

        for (int i=0;i<ftpPaths.size();i++){
            downloadSingle(ftpPaths.get(i), localPath);
            Log.e("----->>>",ftpPaths.get(i));
        }
        Date endTime = new Date();
        result = new Result(flag, Util.getFormatTime(endTime.getTime() - startTime.getTime()), Util.getFormatSize(response));
        return result;
    }



    /**
     * 下载单个文件.
     * @return true下载成功, false下载失败
     * @throws IOException
     */
    private boolean downloadSingle(String path,String locaPath) throws IOException {
        boolean flag = true;
      /*  // 创建输出流
        OutputStream outputStream = new FileOutputStream(localFile);
        // 统计流量
        response += ftpFile.getSize();
        // 下载单个文件
        flag = ftpClient.retrieveFile(localFile.getName(), outputStream);
        // 关闭文件流
        outputStream.close();
        return flag;*/



        int hh=path.lastIndexOf("/");

        makeRootDirectory(path.substring(0,hh));
        Log.e("===="+hh+"======",locaPath+path.substring(0,hh));

        long currentSize = 0;
        // 开始准备下载文件
        Log.e("==========",locaPath+path);

        File localFile=new File(locaPath+path);

        if(!localFile.exists()){
            localFile.createNewFile();
        }

        downNameLinten.oneName(path);
        OutputStream out = new FileOutputStream(localFile);
        //   ftpClient.setRestartOffset(localSize);
        InputStream input = ftpClient.retrieveFileStream(new String(path.getBytes("GBK")));
        if (input != null) {
            Log.e("=======>>", input.toString());
            byte[] b = new byte[1024];
            int length = 0;
            while ((length = input.read(b)) != -1) {
                out.write(b, 0, length);
                currentSize = currentSize + length;

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
        return flag;


    }

    public  void makeRootDirectory(String filePath) {
        File file = null;

        String fi[]=filePath.split("/");

        String hhu="/";
        for (int i=0;i<fi.length;i++) {
            try {

                hhu=hhu+fi[i]+"/";
                Log.e("iiiiiiiiiii---",hhu);
                file = new File("/mnt/sdcard"+hhu);
                if (!file.exists()) {
                    file.mkdir();
                }
            } catch (Exception e) {

            }
        }
    }


    /**
     * 下载多个文件.
     * @return true下载成功, false下载失败
     * @throws IOException
     */
    private boolean downloadMany(String  curPath) throws IOException {


        File ff=new File(curPath);
        if(!ff.exists()) {
            ff.mkdir();
        }
        ftpClient.changeWorkingDirectory(curPath);
        // 得到FTP当前目录下所有文件
        FTPFile[] ftpFiles = ftpClient.listFiles();
        // 循环遍历
        for (FTPFile ftpFile : ftpFiles) {
            if (ftpFile.isDirectory()) {                // 下载多个文件
                downloadMany(curPath+"/"+ftpFile.getName());
            } else {
                ftpPaths.add(curPath+"/"+ftpFile.getName());
            }
        }
        return true;
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


    public interface  DownNameLinten{
        abstract void oneName(String path);
    }

}
