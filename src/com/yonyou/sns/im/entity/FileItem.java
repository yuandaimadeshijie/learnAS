package com.yonyou.sns.im.entity;

import java.io.Serializable;

import android.database.Cursor;
import android.provider.MediaStore;

import com.yonyou.sns.im.R;
import com.yonyou.sns.im.util.common.YMDbUtil;

/**
 * 文件实体
 * @author wudl
 * @date 2014年12月1日
 * @version V1.0
 */
public class FileItem implements Serializable {

	private static final long serialVersionUID = 3900407973571119594L;
	/** words 后缀集*/
	public static final String[] words = { ".docx", ".doc", ".wps" };
	/** excel 后缀集*/
	public static final String[] excels = { ".xlsx", ".xls" };
	/** ppt 后缀集*/
	public static final String[] ppts = { ".ppt", ".pptx" };
	/** pdf 后缀集*/
	public static final String[] pdfs = { ".pdf" };
	/** text 后缀集*/
	public static final String[] txts = { ".txt" };
	/** image 后缀集*/
	public static final String[] images = { ".bmp", ".jpg", ".jpeg", ".png", ".gif" };
	/** media 后缀集*/
	public static final String[] medias = { ".avi", ".rmvb", ".rm", ".asf", ".divx", ".mpg", ".mpeg", ".mpe", ".wmv",
			".mp4", ".mkv", ".vob" };
	/** music 后缀集*/
	public static final String[] musics = { ".wav", ".mp3", ".aif", ".rm", ".wmv", ".mpg4" };
	/** 文件路径*/
	private String filePath;
	/** 文件名*/
	private String fileName;
	/** 文件id*/
	private int fileId;
	/** 文件信息*/
	private String fileInfo;
	/** 文件url*/
	private String fileUrl;
	/** 文件大小*/
	private long size;
	/** 是否选择了文件*/
	private boolean isChecked;

	public FileItem(Cursor cursor) {
		this.filePath = YMDbUtil.getString(cursor, MediaStore.Files.FileColumns.DATA);
		this.fileName = this.filePath.substring(filePath.lastIndexOf("/") + 1, this.filePath.length());
		this.fileId = YMDbUtil.getInt(cursor, MediaStore.Files.FileColumns._ID);
		this.size = YMDbUtil.getLong(cursor, MediaStore.Files.FileColumns.SIZE);
		this.isChecked = false;
	}

	public FileItem(String name, String path, long size) {
		this.fileName = name;
		this.filePath = path;
		this.size = size;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	/**
	 * @return
	 */
	public String getFileInfo() {
		return fileInfo;
	}

	/**
	 * @return
	 */
	public void setFileInfo(String fileInfo) {
		this.fileInfo = fileInfo;
	}

	/**
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return
	 */
	public Long getSize() {
		return size;
	}

	/**
	 * @return
	 */
	public void setSize(Long size) {
		this.size = size;
	}

	/**
	 * @return
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @return
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * @return
	 */
	public int getFileId() {
		return fileId;
	}

	/**
	 * @return
	 */
	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	/**
	 * @return
	 */
	public boolean isChecked() {
		return isChecked;
	}

	/**
	 * @return
	 */
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	/**
	 * 获取文件的icon
	 * @return
	 */
	public static int getFileIcon(String fileName) {
		if (checkExtendsOfFile(fileName, words)) {
			// word
			return R.drawable.local_file_word_icon;
		} else if (checkExtendsOfFile(fileName, excels)) {
			// excel
			return R.drawable.local_file_excel;
		} else if (checkExtendsOfFile(fileName, ppts)) {
			// ppt
			return R.drawable.local_file_ppt;
		} else if (checkExtendsOfFile(fileName, pdfs)) {
			// pdf
			return R.drawable.local_file_pdf;
		} else if (checkExtendsOfFile(fileName, txts)) {
			// txt
			return R.drawable.local_file_txt_file;
		} else if (checkExtendsOfFile(fileName, images)) {
			// img
			return R.drawable.local_file_image_file;
		} else if (checkExtendsOfFile(fileName, musics)) {
			// music
			return R.drawable.local_file_music_file;
		} else if (checkExtendsOfFile(fileName, medias)) {
			// media
			return R.drawable.local_file_video_file;
		}
		return R.drawable.local_file_other;
	}

	/**
	 * 获取文件的icon
	 * @return
	 */
	public int getFileIcon() {
		return getFileIcon(this.fileName);
	}

	/**
	 * 通过文件名判断是什么类型的文件
	 * @param checkItsEnd
	 * @param fileExtends
	 * @return
	 */
	public static boolean checkExtendsOfFile(String fileName, String[] fileExtends) {
		for (String ext : fileExtends) {
			if (fileName.endsWith(ext))
				return true;
		}
		return false;
	}

}
