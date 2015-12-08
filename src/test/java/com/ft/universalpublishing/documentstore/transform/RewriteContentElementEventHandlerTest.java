package com.ft.universalpublishing.documentstore.transform;

import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.universalpublishing.documentstore.util.FixedUriGenerator;
import org.apache.xerces.stax.events.StartElementImpl;
import org.junit.Before;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RewriteContentElementEventHandlerTest {

    public static final QName CONTENT = new QName("content");
    private final DocumentProcessingContext context = new DocumentProcessingContext(FixedUriGenerator.localUriGenerator());

	private RewriteLinkXMLEventHandler unit;

	@Before
	public void before() {
		final Map<String, String> templates = new LinkedHashMap<>();
		templates.put("TEST-TYPE", "/content/{{id}}");
		this.unit = new RewriteLinkXMLEventHandler("ft-content",new UriBuilder(templates), context);
	}

	@Test
	public void testRewrite() throws Exception {
		final StartElement startElement = mockStartElement();
		final EndElement endElement = mockEndContentElement();
		final XMLEventReader xmlEventReader = mock(XMLEventReader.class);
		final BodyWriter eventWriter = mock(BodyWriter.class);

		final Map<String, String> expectedAttributes = new LinkedHashMap<>();
		expectedAttributes.put("type", "TEST-TYPE");
		expectedAttributes.put("url", "http://localhost/content/ABC-123");
		expectedAttributes.put("banana", "BANANA");

		assertFalse(context.isProcessing("content"));
		unit.handleStartElementEvent(startElement, xmlEventReader, eventWriter, context);

		verify(eventWriter).writeStartTag("ft-content", expectedAttributes);

		assertTrue(context.isProcessing("content"));
		unit.handleEndElementEvent(endElement, xmlEventReader, eventWriter);

		verify(eventWriter).writeEndTag("ft-content");

		assertFalse(context.isProcessing("content"));
	}

    private EndElement mockEndContentElement() {
        EndElement element = mock(EndElement.class);
        when(element.getName()).thenReturn(CONTENT);

        return element;
    }

    @Test(expected = BodyTransformationException.class)
	public void testDisallowNesting() throws Exception {
		final StartElement startElement = mockStartElement();
		final XMLEventReader xmlEventReader = mock(XMLEventReader.class);
		final BodyWriter eventWriter = mock(BodyWriter.class);

		context.processingStarted("content"); // Already within a content element
		unit.handleStartElementEvent(startElement, xmlEventReader, eventWriter, context);
	}

	private StartElement mockStartElement() {
		final Attribute type = mockAttribute("type", "TEST-TYPE");
		final Attribute id = mockAttribute("id", "ABC-123");
		final Attribute banana = mockAttribute("banana", "BANANA");

		final List<Attribute> attributes = new ArrayList<>();
		attributes.add(type);
		attributes.add(id);
		attributes.add(banana);

		return new StartElementImpl(CONTENT, attributes.iterator(), null, null, null);
	}

	private Attribute mockAttribute(final String name, final String value) {
		final Attribute attribute = mock(Attribute.class);
		when(attribute.getName()).thenReturn(new QName(name));
		when(attribute.getValue()).thenReturn(value);
		return attribute;
	}
}
