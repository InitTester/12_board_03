package com.pf.www.forum.notice.controller;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.pf.www.forum.message.MessageEnum;
import com.pf.www.forum.notice.dto.BoardAttachDto;
import com.pf.www.forum.notice.dto.BoardDto;
import com.pf.www.forum.notice.message.NoticeMessageEnum;
import com.pf.www.forum.notice.service.BoardService;

@Controller
public class NoticeController {
	private final static Logger log = LoggerFactory.getLogger(NoticeController.class);
	
	@Autowired
	BoardService boardService;

	/* 게시글 리스트 */
	@RequestMapping("/forum//notice/listPage.do")
	public ModelAndView listPage(@RequestParam HashMap<String,String> params, 
			@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size) {
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("key", Calendar.getInstance().getTimeInMillis());
		mv.setViewName("forum/notice/list");

		/* 잘못된 페이지 접근 처리 */
	  	if(page <0 || page > (int)boardService.getTotalListPage(size, page).get("totalPageSize")) {
			mv.addObject("code",MessageEnum.PAGEING_ERROR.getCode());
			mv.addObject("msg",MessageEnum.PAGEING_ERROR.getDescription());	
	  	}
	  	
	  	mv.addObject("list", boardService.getList(params,page,size));
		log.info("page : " + page + ", size : " + size);
	  	mv.addObject("ph", boardService.getTotalListPage(size, page));
	  	
		return mv;
	}
	
	/* 게시글 디테일 */
	@RequestMapping("/forum/notice/readPage.do")
	public ModelAndView readPage(@RequestParam HashMap<String, String> params,
			@RequestParam(defaultValue = "1") Integer boardSeq,
			@RequestParam(defaultValue = "1") Integer boardTypeSeq) {
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("key", Calendar.getInstance().getTimeInMillis());
		mv.setViewName("forum/notice/read");
		
		mv.addObject("board", boardService.getBoardDetail(boardSeq));
		
		String isLike;
		
		try {
			isLike = boardService.getEmptyVote(boardSeq, boardTypeSeq, boardTypeSeq);
		} catch (EmptyResultDataAccessException e) {
			// TODO Auto-generated catch block
			/* 좋아요/싫어요 안한상태 */
			isLike = "";
		}
		
		mv.addObject("isLike", isLike);
		mv.addObject("attFile", boardService.getBoardFileInfo(boardSeq, boardTypeSeq));
		
		return mv;
	}

	/* 게시글 작성 페이지 */
	@RequestMapping("/forum/notice/writePage.do")
	public ModelAndView writePage(@RequestParam HashMap<String, String> params,
			@RequestParam(defaultValue = "1") Integer boardSeq,
			@RequestParam(defaultValue = "1") Integer boardTypeSeq,
			HttpServletRequest request) {

		ModelAndView mv = new ModelAndView();
		HttpSession session = request.getSession();
		
		String memberSeq = "-1";
		
		try {
			memberSeq = session.getAttribute("memberSeq").toString();				
		} 
		
		catch (NullPointerException nep) {
			mv.addObject("code",NoticeMessageEnum.USER_SESSION_FAIL.getCode());
			mv.addObject("msg",NoticeMessageEnum.USER_SESSION_FAIL.getDescription());
			mv.setViewName("/login");
			return mv;
		}

		mv.addObject("key", Calendar.getInstance().getTimeInMillis());
		mv.addObject("board", boardService.getBoardDetail(boardSeq));
		mv.setViewName("forum/notice/write");
		
		return mv;				
	}
	
	/* 게시글 작성 */
	@RequestMapping("/forum/notice/addBoard.do")
	public ModelAndView addBoard(@RequestParam HashMap<String, String> params,
			/* - value = "attFile" 는 jsp에서 input 태그의 name 값이다 
			 * - MultipartFile를 배열로 받는 이유는 한번에 여러 파일을 업로드 받기 위해 배열로 선언한다.
			 * MultipartHttpServletRequest는 배열을 할 수 없다.*/
			@RequestParam(value = "attFile", required= false) MultipartFile[] attFiles,
			HttpServletRequest request) {

		ModelAndView mv = new ModelAndView();		
		HttpSession session = request.getSession();
		
		String memberSeq = "-1";
		
		try {
			memberSeq = session.getAttribute("memberSeq").toString();	
			params.put("memberSeq", memberSeq);
			
		} 
		catch (NullPointerException nep) {
			mv.addObject("code",NoticeMessageEnum.USER_SESSION_FAIL.getCode());
			mv.addObject("msg",NoticeMessageEnum.USER_SESSION_FAIL.getDescription());
//			mv.setViewName("redirect:/login");
			mv.setViewName("login");
			return mv;
		}
		
		params.put("memberSeq",memberSeq);
		
		boolean result = boardService.addBoard(params, attFiles);
		BoardDto dto = new BoardDto().setBoardDto(params);		
		
		// 에러 처리
		mv.addObject("result",result);
		
		if(result) {
			mv.addObject("code",NoticeMessageEnum.SUCCESS.getCode());
			mv.addObject("msg",NoticeMessageEnum.SUCCESS.getDescription());
			mv.setViewName("redirect:/forum/notice/listPage.do");
		} else {	
			mv.addObject("code",NoticeMessageEnum.FAIL.getCode());
			mv.addObject("msg",NoticeMessageEnum.FAIL.getDescription());
			mv.addObject("board", dto);
			mv.setViewName("forum/notice/write");
		}
		
	    return mv;
	}
	
	
	/* 게시글 수정 페이지*/
	@RequestMapping("/forum/notice/modifyPage.do")
	@ResponseBody
	public ModelAndView modifyPage(@RequestParam HashMap<String, String> params,
			@RequestParam(defaultValue = "1") Integer boardSeq,
			@RequestParam(defaultValue = "1") Integer boardTypeSeq) {
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("key",Calendar.getInstance().getTimeInMillis());
		mv.setViewName("forum/notice/modify");
		
		log.info("modifyPage :: boardSeq  : " + boardSeq+ ", boardTypeSeq  : " + boardTypeSeq);
		
		mv.addObject("board",boardService.getBoardDetail(boardSeq));
		
		return mv;
	}
	
	/* 게시글 수정 */
	@RequestMapping("/forum/notice/modify.do")
	public ModelAndView modifty(@RequestParam HashMap<String, String> params,
			HttpServletRequest request) {

		log.info("modifty :: boardSeq  : " + params.get("boardSeq") + ", boardTypeSeq  : " + params.get("boardTypeSeq") );

		ModelAndView mv = new ModelAndView();
		HttpSession session = request.getSession();
		
		String memberSeq = "-1";
		
		try {
			memberSeq = session.getAttribute("memberSeq").toString();	
			
		} 
		catch (NullPointerException nep) {
			mv.addObject("code",NoticeMessageEnum.USER_SESSION_FAIL.getCode());
			mv.addObject("msg",NoticeMessageEnum.USER_SESSION_FAIL.getDescription());
//			mv.setViewName("redirect:/login");
			mv.setViewName("login");
			return mv;
		}

		params.put("memberSeq",memberSeq);
		
		int result = boardService.updateBoard(params);
		BoardDto dto = new BoardDto().setBoardDto(params);
		mv.addObject("result", result);
		
		if(result==1) {
			mv.addObject("code",NoticeMessageEnum.MODIFY_SUCCESS.getCode());
			mv.addObject("msg",NoticeMessageEnum.MODIFY_SUCCESS.getDescription());
			mv.addObject("board",boardService.getBoardDetail(Integer.parseInt(params.get("boardSeq"))));
			mv.setViewName("forum/notice/read");
//			mv.setViewName("redirect:/forum/notice/readPage.do");
		}else {
			
			mv.addObject("code",NoticeMessageEnum.MODIFY_FAIL.getCode());
			mv.addObject("msg",NoticeMessageEnum.MODIFY_FAIL.getDescription());
			mv.addObject("board", dto);
			mv.setViewName("forum/notice/modify");			
		}
		
		return mv;
	}
	
	/* 게시글 삭제 */
	@RequestMapping(value = "/forum/notice/delete.do", method = RequestMethod.GET)//, produces = "application/json")
	@ResponseBody
	public Map<String, Object> delete(@RequestParam HashMap<String, String> params,
			HttpServletRequest request) {
		
		log.info("delete :: boardSeq  : " + params.get("boardSeq") + ", boardTypeSeq  : " + params.get("boardTypeSeq") );

		Map<String, Object> response = new HashMap<>();
		HttpSession session = request.getSession();
		
		String memberSeq = "-1";
		
		try {
			memberSeq = session.getAttribute("memberSeq").toString();				
		} 
		catch (NullPointerException nep) {
			response.put("code",NoticeMessageEnum.USER_SESSION_FAIL.getCode());
			response.put("msg",NoticeMessageEnum.USER_SESSION_FAIL.getDescription());
			response.put("page","/loginPage.do");
			
			return response;
		}

		params.put("memberSeq",memberSeq);
		
		int result = boardService.deleteBoard(params);
		response.put("result", result);
		
		if(result==1) {
			response.put("code",NoticeMessageEnum.DELETE_SUCCESS.getCode());
			response.put("msg",NoticeMessageEnum.DELETE_SUCCESS.getDescription());			
			response.put("page","/forum/notice/listPage.do");
			
		}else {
			response.put("code",NoticeMessageEnum.DELETE_FAIL.getCode());
			response.put("msg",NoticeMessageEnum.DELETE_FAIL.getDescription());
		}
		
		return response;
	}
	
	/* 게시글 파일 다운로드 */
	@GetMapping("forum/download.do")
	public String download( @RequestParam(defaultValue = "1") Integer boardSeq,
							@RequestParam(defaultValue = "1") Integer boardTypeSeq, 
							Model model){
		
		BoardAttachDto dto = boardService.getBoardFileInfo(boardSeq, boardTypeSeq);
		File file = new File(dto.getSavePath());
		
		Map<String, Object> fileInfo = new HashMap<String, Object>();
		fileInfo.put("downloadFile", file);
		fileInfo.put("orgFileNm", dto.getOrgFileNm());
		
		model.addAttribute("fileInfo",fileInfo);
		
		return "fileDownloadView"; // pf-servlet에 등록한 bean 이름으로 간다
	}

	
	
	
	
}
