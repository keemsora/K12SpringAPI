package com.kosmo.k12springapi;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class FileuploadController {
	
	//서버의 물리적 경로 확인하기
		@RequestMapping("/fileUpload/uploadPath.do")
		public void uploadPath(HttpServletRequest req,
					HttpServletResponse resp) throws IOException {
			
			//request객체를 통해 물리적경로를 얻어온다.
			String path = req.getSession().getServletContext().getRealPath("/resources/uploadFile");
			//response객체를 통해 MIME타입을 설정한다.
			resp.setContentType("text/html; charset=utf-8");
			//뷰를 호출하지 않고 컨트롤러에서 내용을 즉시 출력한다.
			PrintWriter pw = resp.getWriter();
			pw.print("/uploadFile 디렉토리의 물리적 경로 : "+path);
		}
		
		//파일업로드 폼
		@RequestMapping("/fileUpload/uploadForm.do")
		public String uploadForm() {
			return "06FileUpload/uploadForm";
		}
		
		/*
		UUID(Universally Unique Identifier)
			: 범용 고유 식별자. randomUUID()를 통해 문자열을 생성하면
			하이픈이 4개 포함된 32자의 랜덤하고 유니크한 문자열이 생성된다.
			JDK에서 기본클래스로 제공된다.
		*/
		public static String getUuid() {
			String uuid = UUID.randomUUID().toString();
			System.out.println("생성된UUID-1:"+uuid);
			uuid = uuid.replaceAll("-", "");
			System.out.println("생성된UUID-2:"+uuid);
			return uuid;
		}
		
		/*
		파일업로드 처리
			: 파일업로드는 반드시 POST방식으로 처리해야 하므로
			컨트롤러 매핑 시 method, value 두 가지 속성을 기술한다.
		*/
		@RequestMapping(value="/fileUpload/uploadAction.do", method=RequestMethod.POST)
		public String uploadAction(Model model, MultipartHttpServletRequest req) {
			
			//서버의 물리적경로 얻어오기
			String path =
					req.getSession().getServletContext().getRealPath("/resources/uploadFile");
			
			//폼값과 파일명을 저장한 후 View로 전달하기 위한 맵 컬렉션
			Map returnObj = new HashMap();
			try {
				//업로드폼의 file속성의 필드를 가져온다.(여기서는 2개임)
				Iterator itr = req.getFileNames();
				
				MultipartFile mfile = null;
				String fileName="";
				List resultList = new ArrayList();
				//파일 외에 폼값 받음
				String title = req.getParameter("title");
				System.out.println("title="+title);
				
				/*
				물리적 경로를 기반으로 File객체를 생성한 후 지정된 디렉토리가
				있는지 확인한다. 만약 없다면 mkdirs()로 생성한다. -> make-directory라는 뜻
				*/
				File directory = new File(path);
				if(!directory.isDirectory()) {
					directory.mkdirs();
				}
				
				//업로드폼의 file필드 갯수만큼 반복
				while(itr.hasNext()) {
					
					//전송된 파일의 이름을 읽어온다.
					fileName = (String)itr.next();
					mfile = req.getFile(fileName);
					System.out.println("mfile="+mfile);
					
					//한글깨짐방지 처리 후 전송된 파일명을 가져온다.
					String originalName =
							new String(mfile.getOriginalFilename().getBytes(), "UTF-8");
					//서버로 전송된 파일이 없다면 while문의 처음으로 돌아간다.
					if("".equals(originalName)) {
						continue;
					}
					
					//파일명에서 확장자를 가져온다.
					String ext =
							originalName.substring(originalName.lastIndexOf('.'));
					//UUID를 통해 생성된 문자열과 확장자를 합쳐서 파일명을 완성한다.
					String saveFileName = getUuid() + ext;
					//물리적 경로에 새롭게 생성된 파일명으로 파일 저장
					File serverFullName =
							new File(path + File.separator + saveFileName);
					
					mfile.transferTo(serverFullName);
					
					Map file = new HashMap();
					//원본 파일명
					file.put("originalName", originalName);
					//저장된 파일명
					file.put("saveFileName", saveFileName);
					//서버의 전체경로
					file.put("serverFullName", serverFullName);
					//제목
					file.put("title", title);
					
					//위 4가지 정보를 저장한 Map을 ArrayList에 저장한다.
					resultList.add(file);
				}
				returnObj.put("files", resultList);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			//모델객체에 리스트 컬렉션을 저장한 후 뷰로 전달
			model.addAttribute("returnObj", returnObj);
			return "06FileUpload/uploadAction";
		}
		
		//파일목록보기
		@RequestMapping("/fileUpload/uploadList.do")
		public String uploadList(HttpServletRequest req, Model model) {
			
			//서버의 물리적경로 얻어오기
			String path = req.getSession().getServletContext().getRealPath("/resources/uploadFile");
			//경로를 기반으로 파일객체 생성
			File file = new File(path);
			//파일의 목록을 배열형태로 얻어온다.
			File[] fileArray = file.listFiles();
			//뷰로 전달할 파일목록을 저장하기 위해 Map생성
			Map<String, Integer> fileMap = new HashMap<String, Integer>();
			for(File f : fileArray) {
				//Map의 key로 파일명, value로 파일용량을 저장
				fileMap.put(f.getName(), (int)Math.ceil(f.length()/1024.0));
			}
			
			model.addAttribute("fileMap", fileMap);
			return "06FileUpload/uploadList";
		}
		
		
		/////////////////////////////////////////////////////////////////////
		//안드로이드에서 업로드 처리
		////////////////////////////////////////////////////////////////////

		@RequestMapping(value="/fileUpload/uploadAndroid.do", method=RequestMethod.POST)
		@ResponseBody
		public Map uploadAndroid(Model model, MultipartHttpServletRequest req) {
			
			//서버의 물리적경로 얻어오기
			String path =
					req.getSession().getServletContext().getRealPath("/resources/uploadFile");
			
			//폼값과 파일명을 저장한 후 View로 전달하기 위한 맵 컬렉션
			Map returnObj = new HashMap();
			try {
				//업로드폼의 file속성의 필드를 가져온다.(여기서는 2개임)
				Iterator itr = req.getFileNames();
				
				MultipartFile mfile = null;
				String fileName="";
				List resultList = new ArrayList();
				//파일 외에 폼값 받음
				String title = req.getParameter("title");
				System.out.println("title="+title);
				
				/*
				물리적 경로를 기반으로 File객체를 생성한 후 지정된 디렉토리가
				있는지 확인한다. 만약 없다면 mkdirs()로 생성한다. -> make-directory라는 뜻
				*/
				File directory = new File(path);
				if(!directory.isDirectory()) {
					directory.mkdirs();
				}
				
				//업로드폼의 file필드 갯수만큼 반복
				while(itr.hasNext()) {
					
					//전송된 파일의 이름을 읽어온다.
					fileName = (String)itr.next();
					mfile = req.getFile(fileName);
					System.out.println("mfile="+mfile);
					
					//한글깨짐방지 처리 후 전송된 파일명을 가져온다.
					String originalName =
							new String(mfile.getOriginalFilename().getBytes(), "UTF-8");
					//서버로 전송된 파일이 없다면 while문의 처음으로 돌아간다.
					if("".equals(originalName)) {
						continue;
					}
					
					//파일명에서 확장자를 가져온다.
					String ext =
							originalName.substring(originalName.lastIndexOf('.'));
					//UUID를 통해 생성된 문자열과 확장자를 합쳐서 파일명을 완성한다.
					String saveFileName = getUuid() + ext;
					//물리적 경로에 새롭게 생성된 파일명으로 파일 저장
					File serverFullName =
							new File(path + File.separator + saveFileName);
					
					mfile.transferTo(serverFullName);
					
					Map file = new HashMap();
					//원본 파일명
					file.put("originalName", originalName);
					//저장된 파일명
					file.put("saveFileName", saveFileName);
					//서버의 전체경로
					file.put("serverFullName", serverFullName);
					//제목
					file.put("title", title);
					
					//위 4가지 정보를 저장한 Map을 ArrayList에 저장한다.
					resultList.add(file);
				}
				
				//파일업로드에 성공했을 때
				returnObj.put("files", resultList);
				returnObj.put("success", 1);//안드로이드 추가부분
			}
			catch(IOException e) {
				//파일업로드에 실패했을 때
				returnObj.put("success", 0);//안드로이드 추가부분
				e.printStackTrace();
			}
			catch(Exception e) {
				returnObj.put("success", 0);//안드로이드 추가부분
				e.printStackTrace();
			}
			
			return returnObj;//안드로이드 추가부분
		}/*
			안드로이드에서 사진을 업로드한 후 결과를 받아야 하므로
			기존 View를 호출하는 부분에서 JSON데이터를 반환하는 형태로
			변경한다.
		*/

}
