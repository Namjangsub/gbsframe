package com.dksys.biz.user.mail.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.dksys.biz.user.mail.mapper.MailMapper;
import com.dksys.biz.user.mail.service.EmailSvc;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;
import java.util.List;
import java.util.Random;

@Service
@Transactional(rollbackFor = Exception.class)
public class EmailSvcImpl implements EmailSvc {
    
    @Autowired
    EmailSvc emailSvc;

    @Autowired
    JavaMailSender javaMailSender;
    
    @Autowired
    SpringTemplateEngine templateEngine;
    
    @Autowired
    MailMapper mailMapper;
    
	@Value("${spring.mail.username}")
	private String USERNAME;
	
	@Value("${spring.mail.password}")
	private String PASSWORD;
	
	@Value("${spring.mail.host}")
	private String HOSTNAME;

	@Value("${spring.mail.port}")
	private String STARTTLS_PORT;

	@Value("${spring.mail.properties.mail.smtp.starttls.enable}")
	private String STARTTLS;
	
	@Value("${spring.mail.properties.mail.smtp.auth}")  
	private String AUTH;
	
	public int sendGmailSimple (Map<String, String> paramMap) throws Exception {
    	String authNum = createCode();
        
        paramMap.put("mailCode", authNum);
//        mailMapper.insertMail(paramMap);
        //office365 용
        
      //Gmail 용
    	SimpleMailMessage simpleMessage = new SimpleMailMessage();
    	
    	simpleMessage.setTo(paramMap.get("mailTo")); // 메일 수신자
    	simpleMessage.setFrom(paramMap.get("mailFrom")); // 메일송신자
    	simpleMessage.setSubject(paramMap.get("mailTitle")); // 메일 제목
    	simpleMessage.setText(paramMap.get("mailCnts")); // 메일 본문 내용, HTML 여부
    	javaMailSender.send(simpleMessage);
    	
    	return 1;
    }
    
    public int sendGmailHtml(Map<String, String> paramMap, MultipartHttpServletRequest mRequest) throws Exception {

        String authNum = createCode();
        // html로 텍스트 설정
        String mailCnts = setHtmlContext(authNum, paramMap);
//        String mailHtml = setContext(authNum, "purchaseOrder.html") // 메일 본문 내용(템플릿 적용), HTML 여부
        
        paramMap.put("mailCnts", mailCnts);
        paramMap.put("mailCode", authNum);
        mailMapper.insertMail(paramMap);
        
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8"); //파일첨부 기능 상용여부 (true:사용, false:미사용)
        String toValue = paramMap.get("mailTo");
        if (toValue != null && !toValue.isEmpty()) {
        	String[] toArr = toValue.split(",");
        	mimeMessageHelper.setTo(toArr); // 메일 수신자
        }
        String ccValue = paramMap.get("mailCc");
        if (ccValue != null && !ccValue.isEmpty()) {
            String[] ccArr = ccValue.split(",");
            mimeMessageHelper.setCc(ccArr); // 메일 참조자
        }
//        toArr = paramMap.get("mailBcc").split(",");
//        mimeMessageHelper.setBcc(toArr); // 메일 숨은참조자
        mimeMessageHelper.setSubject(paramMap.get("mailTitle")); // 메일 제목
        
        // html로 텍스트 설정
        mimeMessageHelper.setText(mailCnts, true);
      
        mimeMessageHelper.addInline("logo", new ClassPathResource("static/img/Logo.png"));//템플릿에 들어가는 이미지 cid로 삽입
 

        List<MultipartFile> fileList = mRequest.getFiles("files");
        // 첨부 파일이 있는 경우에만 처리
    	if (fileList != null && fileList.size() > 0) {
    	    for (MultipartFile mf : fileList) {
    	        String originFileName = mf.getOriginalFilename();
    	        try {
    	            // 파일 이름을 인코딩하여 첨부 파일 추가
    	            String encodedFileName = MimeUtility.encodeText(originFileName, "UTF-8", "B");
    	            mimeMessageHelper.addAttachment(encodedFileName, mf);
    	        } catch (UnsupportedEncodingException e) {
    	            // 파일 이름 인코딩 에러 처리
    	            e.printStackTrace();
    	        }
    	    }
    	}
    	
        javaMailSender.send(mimeMessage);
        
        return 1;
    }

    // 인증번호 및 임시 비밀번호 생성 메서드
    public String createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(4);

            switch (index) {
                case 0: key.append((char) ((int) random.nextInt(26) + 97)); break;
                case 1: key.append((char) ((int) random.nextInt(26) + 65)); break;
                default: key.append(random.nextInt(9));
            }
        }
        return key.toString();
    }

    // thymeleaf를 통한 html 적용
    public String setContext(String code, String type) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process(type, context);
    }
    // html 적용
    public String setHtmlContext(String authNum, Map<String, String> paramMap) {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String dateString = currentDate.format(formatter);
        Map<String, String> userInfo = mailMapper.selectUserInfo(paramMap);

        String mailHtml = "<html><body><table><tbody><tr><td><img src='cid:logo'><p><b><span>발신정보</span></b></p></td></tr>"
				+ "<tr><td><p><span>보낸회사</span></p></td>"
				+ "<td><p><b><span>" + userInfo.get("coCdNm") + "</span></b></p></td>"
				+ "</tr><tr><td><p><span>발행일자</span></p></td>"
				+ "<td><p><b><span>" + dateString + "</span></b></p></td>"
				+ "</tr><tr><td><p><span>Email</span></p></td>"
				+ "<td><p><b><span><a href='mailto:" + paramMap.get("mailFrom") + "'>" + paramMap.get("mailFrom")+ "</a></span></b></p></td>"
				+ "</tr><tr><td><p><span>연락처</span></p></td>"
				+ "<td><p><b><span>" + userInfo.get("offTelNo") + " (fax." + userInfo.get("faxNo") + "</span></b></p></td></tr>"
				+ "<tr><td><p><span >메모</span></p></td>"
				+ "<td><p>" + paramMap.get("mailCnts") + "<br>비밀코드:" + authNum + "</p></td></tr>"
				+ "</tbody></table></body></html>";
    	return mailHtml; 
    }

	@Override
	public int selectMailCount(java.util.Map<String, String> paramMap) {
		return mailMapper.selectMailCount(paramMap);
	}

	@Override
	public List<java.util.Map<String, String>> selectMailList(java.util.Map<String, String> paramMap) {
		return mailMapper.selectMailList(paramMap);
	}

	@Override
	public int insertMail(java.util.Map<String, String> paramMap) {
		return mailMapper.insertMail(paramMap);
	}

	@Override
	public int updateMailError(java.util.Map<String, String> paramMap) {
		return mailMapper.updateMailError(paramMap);
	}

	@Override
	public int deleteMail(java.util.Map<String, String> paramMap) {
		return mailMapper.deleteMail(paramMap);
	}
	
	Properties properties;
	Session session;
	MimeMessage mimeMessage;
	
	public int sendMailSimple(Map<String, String> paramMap) {
		try {
			properties = new Properties();
			properties.put("mail.smtp.host", HOSTNAME);
			// Setting STARTTLS_PORT
			properties.put("mail.smtp.port", STARTTLS_PORT);
			// AUTH enabled
			properties.put("mail.smtp.auth", AUTH);
			// STARTTLS enabled
			properties.put("mail.smtp.starttls.enable", STARTTLS);

			// Authenticating
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(USERNAME, PASSWORD);
				}
			};

			// creating session
			session = Session.getInstance(properties, auth);

			// create mimemessage
			mimeMessage = new MimeMessage(session);
			
			//from address should exist in the domain
			mimeMessage.setFrom(new InternetAddress(paramMap.get("mailFrom")));
			mimeMessage.addRecipient(RecipientType.TO, new InternetAddress(paramMap.get("mailTo")));
			mimeMessage.setSubject(paramMap.get("mailTitle"));

			// setting text message body
			mimeMessage.setText(paramMap.get("mailCnts"));

			// sending mail
			Transport.send(mimeMessage);
			System.out.println("Mail Send Successfully");
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}	
	
	public int sendMailHtml(Map<String, String> paramMap, MultipartHttpServletRequest mRequest ) throws Exception {
		try {
			properties = new Properties();
			properties.put("mail.smtp.host", HOSTNAME);
			// Setting STARTTLS_PORT
			properties.put("mail.smtp.port", STARTTLS_PORT);
			// AUTH enabled
			properties.put("mail.smtp.auth", AUTH);
			// STARTTLS enabled
			properties.put("mail.smtp.starttls.enable", STARTTLS);

			// Authenticating
			Authenticator auth = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(USERNAME, PASSWORD);
				}
			};

			// creating session
			session = Session.getInstance(properties, auth);

			// create mime message
			mimeMessage = new MimeMessage(session);
			
			mimeMessage.setFrom(new InternetAddress(paramMap.get("mailFrom")));
			mimeMessage.addRecipient(RecipientType.TO, new InternetAddress(paramMap.get("mailTo")));
			mimeMessage.setSubject(paramMap.get("mailTitle"));
			
			// Multipart 객체 생성하여 본문과 첨부 파일 추가
			Multipart multipart = new MimeMultipart();

			// setting HTML message body
			MimeBodyPart bodyPart = new MimeBodyPart();
			
	        String authNum = createCode();
	        // html로 텍스트 설정
	        String mailCnts = setHtmlContext(authNum, paramMap);
			
            bodyPart.setContent(mailCnts, "text/html; charset=utf-8");
            multipart.addBodyPart(bodyPart);

            // 첨부 파일 추가
            List<MultipartFile> fileList = mRequest.getFiles("files");
            if (fileList != null && fileList.size() > 0) {
                for (MultipartFile mf : fileList) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    DataSource source = new ByteArrayDataSource(mf.getBytes(), mf.getContentType());
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(mf.getOriginalFilename());
                    multipart.addBodyPart(attachmentPart);
                }
            }
            // 클래스 패스에 위치한 이미지를 cid로 추가
            MimeBodyPart imagePart = new MimeBodyPart();
            DataSource fds = new FileDataSource(new ClassPathResource("static/img/Logo.png").getFile());
            imagePart.setDataHandler(new DataHandler(fds));
            imagePart.setHeader("Content-ID", "logo");
            multipart.addBodyPart(imagePart);

            mimeMessage.setContent(multipart);
		
			// sending mail
			Transport.send(mimeMessage);
			System.out.println("Mail Send Successfully");
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

}