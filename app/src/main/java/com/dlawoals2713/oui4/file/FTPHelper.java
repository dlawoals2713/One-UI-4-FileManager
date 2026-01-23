package com.dlawoals2713.oui4.file;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FTPHelper {
    private static final String TAG = "FTPHelper";
    private FTPClient ftpClient;
    private String server;
    private int port;
    
    public FTPHelper() {
        ftpClient = new FTPClient();
        // 타임아웃 및 인코딩 설정
        ftpClient.setConnectTimeout(10000);
        ftpClient.setDataTimeout(10000);
        ftpClient.setDefaultTimeout(10000);
        ftpClient.setControlEncoding("UTF-8"); // UTF-8 인코딩 설정
    }

    public boolean connect(String server, int port, String user, String password) {
        this.server = server;
        this.port = port;
        try {
            ftpClient.setAutodetectUTF8(true); // <- 먼저!
            ftpClient.connect(server, port);
    
            ftpClient.setControlEncoding("UTF-8"); // UTF-8 설정
            boolean success = ftpClient.login(user, password);
            
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            return success;
        } catch (IOException e) {
            Log.e(TAG, "Connection error", e);
            disconnect();
            return false;
        }
    }

    public boolean downloadFile(String remoteFilePath, String localFilePath, 
                              DownloadProgressListener listener) throws IOException {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        
        try {
            // 원격 파일 정보 확인 (한글 파일명 처리)
            FTPFile[] files = ftpClient.listFiles(remoteFilePath);
            if (files == null || files.length == 0 || files[0] == null) {
                throw new IOException("파일을 찾을 수 없음: " + remoteFilePath);
            }
            
            FTPFile remoteFile = files[0];
            if (remoteFile.getSize() <= 0) {
                throw new IOException("유효하지 않은 파일 크기");
            }

            outputStream = new BufferedOutputStream(new FileOutputStream(localFilePath));
            inputStream = ftpClient.retrieveFileStream(remoteFilePath);
            
            if (inputStream == null) {
                throw new IOException("파일 스트림을 가져올 수 없음: " + ftpClient.getReplyString());
            }

            byte[] buffer = new byte[8192];
            long totalBytesRead = 0;
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                
                if (listener != null) {
                    listener.onProgress(totalBytesRead, remoteFile.getSize());
                }
            }
            
            outputStream.flush();
            return ftpClient.completePendingCommand();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing streams", e);
            }
        }
    }

    public List<FTPFile> getFileList() {
        List<FTPFile> files = new ArrayList<>();
        try {
            FTPFile[] ftpFiles = ftpClient.listFiles();
            if (ftpFiles != null) {
                for (FTPFile file : ftpFiles) {
                    // "."과 ".." 디렉토리 항목 제외 및 유효성 검사
                    if (file != null && file.getName() != null && 
                        !file.getName().equals(".") && !file.getName().equals("..")) {
                        files.add(file);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting file list", e);
        }
        return files;
    }

    public boolean changeDirectory(String path) throws IOException {
        return ftpClient.changeWorkingDirectory(path);
    }

    public boolean goToParentDirectory() throws IOException {
        return ftpClient.changeToParentDirectory();
    }

    public void disconnect() {
        new Thread(() -> {
            try {
                if (ftpClient != null) {
                    if (ftpClient.isConnected()) {
                        ftpClient.logout();
                    }
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                Log.e(TAG, "Disconnection error", e);
            } finally {
                ftpClient = null;
            }
        }).start();
    }

    public String getCurrentDirectory() {
        try {
            return ftpClient.printWorkingDirectory();
        } catch (IOException e) {
            Log.e(TAG, "Error getting current directory", e);
            return "/";
        }
    }

    public boolean isConnected() {
        return ftpClient != null && ftpClient.isConnected();
    }

    public interface DownloadProgressListener {
        void onProgress(long bytesRead, long totalBytes);
    }
    
    public String getLastReply() {
        return ftpClient.getReplyString();
    }
    
    // FTPHelper.java
    public String getServerAddress() {
        return server + ":" + port;  // 예: "example.com:21"
    }
    
    // 파일 또는 디렉토리 경로를 받아 총 크기를 계산하는 정적 메서드
    public long getFtpFileSize(String remotePath) {
        long totalSize = 0;
        try {
            FTPFile[] files = ftpClient.listFiles(remotePath);
    
            if (files.length == 1 && files[0].isFile()) {
                // 단일 파일일 경우
                totalSize += files[0].getSize();
            } else {
                // 디렉토리일 경우
                if (!ftpClient.changeWorkingDirectory(remotePath)) {
                    return 0;
                }
                FTPFile[] dirFiles = ftpClient.listFiles();
                for (FTPFile file : dirFiles) {
                    if (file.getName().equals(".") || file.getName().equals("..")) continue;
    
                    String childPath = remotePath + "/" + file.getName();
                    if (file.isDirectory()) {
                        totalSize += getFtpFileSize(childPath); // 재귀 호출
                    } else {
                        totalSize += file.getSize();
                    }
                }
                // 원래 디렉토리로 복귀 (안정성)
                ftpClient.changeToParentDirectory();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting FTP file size: " + remotePath, e);
        }
        return totalSize;
    }
}