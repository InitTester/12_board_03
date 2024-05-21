package com.pf.www.forum.notice.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


@Component
public class FileUtil {
	private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
	/* 날짜 지정 
	 * 한 폴더에 파일들이 있다면 비효율 적이고 추후 관리가 힘들다.*/
	String savePathDay = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
	
	/* window 기준 임시 경로 
	 * 각각의 pc 위치를 가져오려면 
	 * Path root = Paths.get(System.getProperty("user.name"));
	 * 을 이용해서 경로 설정도 가능하다.*/
	
	private String SAVE_PATH = "C:/dev/tmp/" + savePathDay;
	
	public File saveFile(MultipartFile mf) {
		
		File destFile = new File(SAVE_PATH);
		
		log.info("saveFile :: getAbsolutePath : "+ destFile.getAbsolutePath());
		
		// 해당 경로가 없다면 생성하자
		if(!destFile.exists()) {
			/* mkdirs() : 생성 폴더 상위 디렉토리가 없으면 상위 디렉토리까지 생성
			 * mkdir() : 생성 폴더 상위 디렉토리가 없으면 생성불가 */
			destFile.mkdirs();
		}
		
		// 파일명 중복을 피하기 위해 UUID 생성
		destFile = new File(SAVE_PATH, UUID.randomUUID().toString().replaceAll("-", ""));
		
		// 보통확장자는 맨마지막에 위치 해서 잘라서 사용가능
		log.info("saveFile :: getAbsolutePath : " + destFile.getAbsolutePath() +
				                    ", exists : " + destFile.exists() + 
				                    ", length : " + destFile.length());
		
		/* Path p = null; */
		
		try {
			mf.transferTo(destFile);
			log.info("saveFile :: exists : " + destFile.exists() + 
				               ", length : " + destFile.length());
		} catch (IllegalStateException e) {
			
			// TODO Auto-generated catch block			
			e.printStackTrace();
		} catch (IOException e) {
			
			// TODO Auto-generated catch block			
			e.printStackTrace();
		}
		return destFile;
	}
	
	/* java 에서는 대표적으로 파일 저장하는 방법 중 Path와 File 클래스를 사용해서 저장하는 방법이 있다.
	 * https://initsave.tistory.com/378 참고
	 */
	
}
