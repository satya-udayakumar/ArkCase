package com.armedia.acm.service;

/*-
 * #%L
 * ACM Service: File Converting Service
 * %%
 * Copyright (C) 2014 - 2019 ArkCase LLC
 * %%
 * This file is part of the ArkCase software. 
 * 
 * If the software was purchased under a paid ArkCase license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.tidy.Tidy;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MSGToPDFConverter implements FileConverter {
	private final Logger log = LogManager.getLogger(getClass().getName());

	@Override
	public File convert(InputStream fileInputStream, String fileName) {
		String timestamp = String.valueOf(new Timestamp(System.currentTimeMillis()).getTime());
		String tmpPdfFilePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(fileName).concat("_")
				.concat(timestamp).concat(".pdf");

		File pdfFile = new File(tmpPdfFilePath);

		try (OutputStream fos = new FileOutputStream(tmpPdfFilePath)) {
			MAPIMessage message = new MAPIMessage(fileInputStream);
			createPdf(message, fos);
		} catch (Exception e) {
			log.error(String.format("File [%s] could not be converted to PDF", fileName), e);
			return null;
		}

		return pdfFile;
	}

	private void createPdf(MAPIMessage msg, OutputStream fos) throws DocumentException, ChunkNotFoundException {
		String fromEmail = "";
		try
		{
			fromEmail = msg.getDisplayFrom();
		}
		catch (ChunkNotFoundException e)
		{
			log.debug("Sender email address is empty");
		}
		fromEmail = fromEmail.contains("Content_Types") ? "" : fromEmail;
		String toEmail = ""; 
		try 
		{
			toEmail = msg.getDisplayTo();
		}
		catch (ChunkNotFoundException e)
		{
			log.debug("Receiver email address is empty");
		}
		String toCC = "";
		try 
		{
			toCC = msg.getDisplayCC();
		}
		catch (ChunkNotFoundException e)
		{
			log.debug("CC Receiver email address is empty");
		}
		String subject = "";
		try 
		{
			subject = msg.getSubject();
		}
		catch (ChunkNotFoundException e)
		{
			log.debug("Subject is empty");
		}
		String bodyRtf = "";
		String bodyHTML = "";
		String bodyText = "";
		try {
			bodyRtf = msg.getRtfBody();
		} catch (ChunkNotFoundException e) {
			try {
				bodyHTML = msg.getHtmlBody();
			} catch (ChunkNotFoundException e1) {
				try {
					bodyText = msg.getTextBody();	
				}
				catch (ChunkNotFoundException e2)
				{
					log.debug("Body is empty");
				}
			}
		}

		Document document = new Document();
		try {
			PdfWriter.getInstance(document, fos);
			document.open();
			document.add(new Paragraph(MessageFormat.format("From: {0}", fromEmail)));
			document.add(new Paragraph(MessageFormat.format("To: {0}", toEmail)));
			document.add(new Paragraph(MessageFormat.format("Cc: {0}", toCC)));
			document.add(new Paragraph("Subject: " + subject));
			if (!StringUtils.isEmpty(bodyRtf)) {
				if (!parseRtf(bodyRtf, document)) {
					document.add(new Paragraph("Body text: "));
					document.add(new Paragraph(bodyText));
				}
			} else if (!StringUtils.isEmpty(bodyHTML)) {
				if (!parseHtml(bodyHTML, document)) {
					document.add(new Paragraph("Body text: "));
					document.add(new Paragraph(bodyText));
				}
			} else {
				document.add(new Paragraph("Body text: "));
				document.add(new Paragraph(bodyText));
			}
		} finally {
			document.close();
		}
	}

	private boolean parseRtf(String bodyRtf, Document document) {
		try {
			AutoDetectParser parser = new AutoDetectParser();
			Metadata metadata = new Metadata();

			StringWriter sw = new StringWriter();
			SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
			TransformerHandler handler = factory.newTransformerHandler();
			handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "xml");
			handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "no");
			handler.setResult(new StreamResult(sw));

			parser.parse(new ByteArrayInputStream(bodyRtf.getBytes()), handler, metadata, new ParseContext());

			String xhtml = sw.toString();

			ElementList list = XMLWorkerHelper.parseToElementList(xhtml, null);
			document.add(new Paragraph("Body RTF: "));
			for (Element element : list) {
				document.add(element);
			}
		} catch (Exception e) {
			log.error("Failed to parse RTF email body!", e);
			return false;
		}

		return true;
	}

	private boolean parseHtml(String bodyHTML, Document document) {
		bodyHTML = fixSmallFonSize(bodyHTML);
		bodyHTML = fixRemToPx(bodyHTML);
		bodyHTML = fixBlockQuoteTag(bodyHTML);
		bodyHTML = fixBlockElements(bodyHTML);
		bodyHTML = fixSmartQuotes(bodyHTML);
		// convert to correct XHTML
		ByteArrayOutputStream xhtmlOutputStream = new ByteArrayOutputStream();
		Tidy tidy = new Tidy();
		//tidy.setShowWarnings(true);
		tidy.setInputEncoding("UTF-8");
		tidy.setOutputEncoding("UTF-8");
		tidy.setXHTML(true);
		tidy.setMakeClean(true);
		tidy.setMakeBare(true);
		tidy.setDropProprietaryAttributes(true);
		tidy.setForceOutput(true);
		tidy.parse(new ByteArrayInputStream(bodyHTML.getBytes()), xhtmlOutputStream);

		try {
			ElementList list = XMLWorkerHelper
					.parseToElementList(new String(xhtmlOutputStream.toByteArray(), StandardCharsets.UTF_8), null);
			for (Element element : list) {
				document.add(element);
			}
		} catch (Exception e) {
			log.error("Failed to parse HTML email body!", e);
			return false;
		}

		return true;
	}
	
	private String fixSmallFonSize(String bodyHTML) {
		return bodyHTML.replaceAll("font-size:\\s*?0px", "font-size:1px");
	}
	
	private String fixRemToPx(String bodyHTML)
	{
		Pattern p = Pattern.compile("font-size:\\s*(\\d+)rem|font-size:?(\\s*\\d{0,}\\.\\d{1,2})rem");
		Matcher m = p.matcher(bodyHTML);
		Map<String, String> replacements = new HashMap<>();
		while (m.find()) {
			String group = m.group(1) == null ? m.group(2) : m.group(1);
			double num = Double.parseDouble(group);
			double remInPx = num*16;
			if(!replacements.keySet().contains(m.group()))
			{
				replacements.put(m.group(), "font-size:" + remInPx + "px");
			}
		}
		for(Map.Entry<String, String> entry : replacements.entrySet())
		{
			bodyHTML = bodyHTML.replaceAll(entry.getKey(), entry.getValue());
		}
		return bodyHTML;
	}
	
	private String fixBlockQuoteTag(String bodyHTML)
	{
		return bodyHTML.replaceAll("<blockquote", "<div").replaceAll("</blockquote", "</div");
	}
	
	private String fixBlockElements(String bodyHTML)
	{
		return bodyHTML.replaceAll("<!\\[if.*\\]>", "").replaceAll("<!\\[endif\\]>", "");
	}
	
	private String fixSmartQuotes(String bodyHTML)
	{
		return bodyHTML.replaceAll("â€™", "\'").replaceAll("â€“", "-").replaceAll("â€œ","\"").replaceAll("â€�","\"");
	}
}
