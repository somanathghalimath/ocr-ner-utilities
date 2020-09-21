package com.nlp.proto.NLPCoreService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@RestController
public class NLPCoreService {

	@PostMapping("/srvc/redact")
	public @ResponseBody ResponseEntity<byte[]> redact(@RequestParam("file") MultipartFile file) {
		
		byte[] contents = redactorService(file);
		
		
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_PDF);
	    String filename = "output.pdf";
	    headers.setContentDispositionFormData(filename, filename);
	    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
	    ResponseEntity<byte[]> response = new ResponseEntity<>(contents, headers, HttpStatus.OK);
	    return response;
	}
	
	private static Tesseract getTesseract() {
		Tesseract instance = new Tesseract();
		instance.setDatapath("C:\\Users\\somna\\OneDrive\\Documents\\Tess4J-3.4.8-src\\Tess4J\\tessdata");
		instance.setLanguage("eng");
		return instance;
	}

	private byte[] redactorService(MultipartFile multipartFile) {

		Tesseract tesseract = getTesseract();
		File file = new File("C:\\Users\\somna\\OneDrive\\Documents\\input.pdf");

		String result = "";
		try {
			multipartFile.transferTo(file);
			result = tesseract.doOCR(file);
		} catch (TesseractException | IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(result);

		List<String> redactedsentences = extractEntities(result);
		redactedsentences.forEach((entry) -> System.out.println(entry));
		byte [] docbytearray = null;
		try {
			docbytearray = createPDF(redactedsentences);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return docbytearray;
	}

	private static byte[] createPDF(List<String> redactedsentences) throws FileNotFoundException, IOException {
		
		try {
			Document document = new Document();
			PdfWriter.getInstance(document, new FileOutputStream("C:\\Users\\somna\\OneDrive\\Documents\\iTextHelloWorld.pdf"));
			 
			document.open();
			Paragraph text = new Paragraph("");
			Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, BaseColor.DARK_GRAY);
			for(String sentence: redactedsentences) {
				text = new Paragraph(sentence,font);
				document.add(text);
				//document.add(Chunk.NEWLINE);
			}
			document.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new FileInputStream("C:\\Users\\somna\\OneDrive\\Documents\\iTextHelloWorld.pdf").readAllBytes();

	}

	private static List<String> extractEntities(String result) {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.put("ssplit.eolonly", "true");

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		Annotation document = new Annotation(result);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

		List<String> resultList = new ArrayList<String>();
		for (CoreMap sentence : sentences) {
	        

			List<String> words = new ArrayList<String>();
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

				String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
				String currentEntity = token.word();

				if (!"O".equals(ne) && ("LOCATION".equals(ne) || "ORGANIZATION".equals(ne) || "PERSON".equals(ne) || "DATE".equals(ne) || "NATIONALITY".equals(ne) || "CITY".equals(ne) || "COUNTRY".equals(ne) || "MISC".equals(ne))) {
					words.add("xxxxx");
				} else {
					words.add(currentEntity);
				}
			
			}
			
			int sentenceOffsetStart = sentence.get(CharacterOffsetBeginAnnotation.class);
	        if (sentenceOffsetStart > 1 && result.substring(sentenceOffsetStart - 2, sentenceOffsetStart).equals("\n\n")) {
	        	resultList.add(" ");
	        }
	        resultList.add(DetokenizerUtil.deTokenize(Arrays.copyOf(words.toArray(), words.toArray().length,String[].class)));
		}
		return resultList;
	}

}
