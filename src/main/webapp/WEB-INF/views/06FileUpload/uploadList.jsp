<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link rel="stylesheet" href="../common/bootstrap4.5.3/css/bootstrap.css" />
<script src="../common/jquery/jquery-3.5.1.js"></script>
</head>
<body>
<div class="container">
    <h2>upload폴더의 파일목록 보기</h2>

    <ul>
    <c:forEach items="${fileMap }" var="file" varStatus="vs">   	 
   	 <li>
   		 파일명 : ${file.key }
   		 &nbsp;&nbsp;
   		 파일크기 : ${file.value }Kb
   		 &nbsp;&nbsp;
   		 <a href="download.do?fileName=${file.key }&oriFileName=임시파일명${vs.count }.jpg">
   			 [다운로드]
   		 </a>
   		 <!-- 다운로드시 원본파일명으로 변경하려면 기존
   		 파일명을 DB에 저장해야 하므로, 여기서는 임시로
   		 파일명을 지정한다.  -->
   	 </li>
    </c:forEach>    
    </ul>
</div>

</html>