package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.read.Uri;
import com.ft.universalpublishing.documentstore.model.transformer.AlternativeTitles;
import com.ft.universalpublishing.documentstore.model.transformer.Brand;
import com.ft.universalpublishing.documentstore.model.transformer.Comments;
import com.ft.universalpublishing.documentstore.model.transformer.Content;
import com.ft.universalpublishing.documentstore.model.transformer.Copyright;
import com.ft.universalpublishing.documentstore.model.transformer.Identifier;
import com.ft.universalpublishing.documentstore.model.transformer.Member;

import com.ft.universalpublishing.documentstore.model.transformer.Standout;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ContentMapperTest {

    private final ContentMapper mapper = new ContentMapper(new IdentifierMapper(),
            new TypeResolver(),
            new BrandsMapper(),
            new StandoutMapper(),
            "localhost");

    @Test
    public void testContentMapping() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Date publishDate = new Date();
        final Date lastModified = new Date();
        final AlternativeTitles alternativeTitles = AlternativeTitles.builder().withPromotionalTitle("promotionalTitle").build();
        final Copyright copyright = new Copyright("notice");
        final SortedSet<Member> members=new TreeSet<>();
        members.add(new Member("uuid"));
        final SortedSet<Identifier> identifiers = new TreeSet<>();
        identifiers.add(new Identifier("authority1", "identifier1"));
        final SortedSet<Brand> brands = new TreeSet<>();
        brands.add(new Brand("Lex"));
        brands.add(new Brand("Chuck Taylor"));
        final UUID mainImageUuid = UUID.randomUUID();
        final Standout standout = new Standout(true, true, true);
        final Content content = Content.builder()
                .withUuid(uuid)
                .withTitle("Philosopher")
                .withPublishedDate(publishDate)
                .withBody("Why did the chicken cross the street?")
                .withOpening("Why did the chicken")
                .withByline("David Jules")
                .withBrands(brands)
                .withMainImage(mainImageUuid.toString())
                .withIdentifiers(identifiers)
                .withComments(new Comments(true))
                .withPublishReference("Publish Reference")
                .withLastModifiedDate(lastModified)
                .withStandout(standout)
                .withCopyright(copyright)
                .withMembers(members)
                .withStandfirst("standfirst")
                .withAlternativeTitles(alternativeTitles)
                .build();

        final com.ft.universalpublishing.documentstore.model.read.Content readContent = mapper.map(content);
        final Uri expectedUri=new Uri("http://localhost/content/uuid");

        assertThat(readContent.getId(), equalTo("http://www.ft.com/thing/" + uuid.toString()));
        assertThat(readContent.getTitle(), equalTo("Philosopher"));
        assertThat(readContent.getStandfirst(), equalTo("standfirst"));
        assertThat(readContent.getPublishedDate(), equalTo(new DateTime(publishDate.getTime())));
        assertThat(readContent.getType(), equalTo(TypeResolver.TYPE_ARTICLE));
        assertThat(readContent.getBodyXML(), equalTo("Why did the chicken cross the street?"));
        assertThat(readContent.getOpeningXML(), equalTo("Why did the chicken"));
        assertThat(readContent.getByline(), equalTo("David Jules"));
        assertThat(readContent.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        final SortedSet<String> expectedBrands = new TreeSet<>();
        expectedBrands.add("Lex");
        expectedBrands.add("Chuck Taylor");
        assertThat(readContent.getBrands(), equalTo(expectedBrands));
        assertThat(readContent.getMainImage(), equalTo(new Uri("http://localhost/content/" + mainImageUuid.toString())));
        assertThat(readContent.getComments(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Comments(true)));
        assertThat(readContent.getPublishReference(), equalTo("Publish Reference"));
        assertThat(readContent.getLastModified(), equalTo(new DateTime(lastModified.getTime())));
        assertThat(readContent.getStandout(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Standout(true, true, true)));
        assertThat((readContent.getAlternativeTitles()).getPromotionalTitle(), equalTo("promotionalTitle"));
        assertThat((readContent.getCopyright()).getNotice(), equalTo("notice"));
        assertThat(((readContent.getMembers()).first()).getId(), equalTo(expectedUri.getId()));
    }
    
    
    @Test
    public void testContentMappingWithNulls() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Date publishDate = new Date();
        final Date lastModified = new Date();
        final AlternativeTitles alternativeTitles = AlternativeTitles.builder().build();
        final SortedSet<Identifier> identifiers = new TreeSet<>();
        identifiers.add(new Identifier("authority1", "identifier1"));
        final SortedSet<Brand> brands = new TreeSet<>();
        brands.add(new Brand("Lex"));
        brands.add(new Brand("Chuck Taylor"));
        final UUID mainImageUuid = UUID.randomUUID();
        final Standout standout = new Standout(true, true, true);
        final Content content = Content.builder()
                .withUuid(uuid)
                .withTitle(null)
                .withPublishedDate(publishDate)
                .withBody("Why did the chicken cross the street?")
                .withOpening("Why did the chicken")
                .withByline(null)
                .withBrands(null)
                .withPublishReference(null)             
                .withStandout(null)
                .withStandfirst(null)
                .withMainImage(null)
                .withIdentifiers(null)
                .withComments(null)
                .withMembers(null)
                .withCopyright(null)
                .withLastModifiedDate(null)
                .withAlternativeTitles(alternativeTitles)
                .build();

        final com.ft.universalpublishing.documentstore.model.read.Content readContent = mapper.map(content);

        assertThat(readContent.getId(), equalTo("http://www.ft.com/thing/" + uuid.toString()));
        assertThat(readContent.getPublishedDate(), equalTo(new DateTime(publishDate.getTime())));;
        assertThat(readContent.getType(), equalTo(TypeResolver.TYPE_ARTICLE));
        assertThat(readContent.getBodyXML(), equalTo("Why did the chicken cross the street?"));
        assertThat(readContent.getOpeningXML(), equalTo("Why did the chicken"));
        assertThat(readContent.getTitle(), nullValue());
        assertThat(readContent.getStandfirst(), nullValue());
        assertThat(readContent.getByline(), nullValue());
        assertThat(readContent.getBrands(), nullValue());
        assertThat(readContent.getPublishReference(), nullValue());
        assertThat(readContent.getIdentifiers(),nullValue());
        assertThat(readContent.getMainImage(),nullValue());
        assertThat(readContent.getComments(), nullValue());
        assertThat(readContent.getLastModified(),nullValue());
        assertThat(readContent.getMembers(), nullValue());
        assertThat(readContent.getCopyright(), nullValue());
        assertThat(readContent.getStandout(),nullValue());
        assertThat((readContent.getAlternativeTitles()).getPromotionalTitle(), nullValue());
    }




    @Test
    public void testLiveBlogContentMapping() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Date publishDate = new Date();
        final String title = "Philosopher";
        final String byline = "David Jules";

        final SortedSet<Identifier> identifiers = new TreeSet<>();
        identifiers.add(new Identifier("authority1", "identifier1"));

        final SortedSet<Brand> brands = new TreeSet<>();
        brands.add(new Brand("Lex"));
        brands.add(new Brand("Chuck Taylor"));

        final UUID mainImageUuid = UUID.randomUUID();

        final String ref = "Publish Reference";

        final Content content = Content.builder()
                .withUuid(uuid)
                .withTitle(title)
                .withPublishedDate(publishDate)
                .withByline(byline)
                .withBrands(brands)
                .withMainImage(mainImageUuid.toString())
                .withIdentifiers(identifiers)
                .withComments(new Comments(true))
                .withRealtime(true)
                .withPublishReference(ref)
                .build();

        final com.ft.universalpublishing.documentstore.model.read.Content readContent = mapper.map(content);

        assertThat(readContent.getId(), equalTo("http://www.ft.com/thing/" + uuid.toString()));
        assertThat(readContent.getTitle(), equalTo("Philosopher"));
        assertThat(readContent.getPublishedDate(), equalTo(new DateTime(publishDate.getTime())));
        assertThat(readContent.getType(), equalTo(TypeResolver.TYPE_ARTICLE));
        assertThat(readContent.getBodyXML(),nullValue());
        assertThat(readContent.getByline(), equalTo("David Jules"));
        assertThat(readContent.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        final SortedSet<String> expectedBrands = new TreeSet<>();
        expectedBrands.add("Lex");
        expectedBrands.add("Chuck Taylor");
        assertThat(readContent.getBrands(), equalTo(expectedBrands));
        assertThat(readContent.getMainImage(), equalTo(new Uri("http://localhost/content/" + mainImageUuid.toString())));
        assertThat(readContent.getComments(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Comments(true)));
        assertThat(readContent.getPublishReference(), equalTo("Publish Reference"));

        assertThat(readContent.isRealtime(),equalTo(true));
    }
    
    @Test
    public void testImageMapping() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Date publishDate = new Date();
        final Date lastModified = new Date();
        final SortedSet<Identifier> identifiers = new TreeSet<>();
        identifiers.add(new Identifier("authority1", "identifier1"));
        final Content content = Content.builder()
                .withUuid(uuid)
                .withTitle("Philosopher")
                .withPublishedDate(publishDate)
                .withDescription("A question.")
                .withByline("David Jules")
                .withInternalBinaryUrl("http://methode-image-binary-transformer/binary/" + uuid.toString())
                .withExternalBinaryUrl("http://ft.s3.aws/" + uuid.toString())
                .withIdentifiers(identifiers)
                .withLastModifiedDate(lastModified)
                .withPixelWidth(1536)
                .withPixelHeight(1538)
                .withCopyright(new Copyright("© AFP"))
                .build();

        final com.ft.universalpublishing.documentstore.model.read.Content readContent = mapper.map(content);

        assertThat(readContent.getId(), equalTo("http://www.ft.com/thing/" + uuid.toString()));
        assertThat(readContent.getTitle(), equalTo("Philosopher"));
        assertThat(readContent.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        assertThat(readContent.getType(), equalTo(TypeResolver.TYPE_MEDIA_RESOURCE));
        assertThat(readContent.getByline(), equalTo("David Jules"));
        assertThat(readContent.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        assertThat(readContent.getBinaryUrl() , equalTo("http://ft.s3.aws/" + uuid.toString()));
        assertThat(readContent.getPublishedDate(), equalTo(new DateTime(publishDate.getTime())));
        assertThat(readContent.getLastModified(), equalTo(new DateTime(lastModified.getTime())));
        assertThat(readContent.getPixelWidth() , equalTo(1536));
        assertThat(readContent.getPixelHeight() , equalTo(1538));
        assertThat(readContent.getCopyright().getNotice(),equalTo("© AFP"));
    }

    @Test
    public void testImageSetMapping() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Date publishDate = new Date();
        final Date lastModified = new Date();
        final SortedSet<Identifier> identifiers = new TreeSet<>();
        identifiers.add(new Identifier("authority1", "identifier1"));
        final SortedSet<Member> members = new TreeSet<>();
        final UUID memberUuid = UUID.randomUUID();
        members.add(new Member(memberUuid.toString()));
        final Content content = Content.builder()
                .withUuid(uuid)
                .withTitle("Philosopher")
                .withPublishedDate(publishDate)
                .withDescription("A question.")
                .withByline("David Jules")
                .withMembers(members)
                .withIdentifiers(identifiers)
                .withLastModifiedDate(lastModified)
                .build();

        final com.ft.universalpublishing.documentstore.model.read.Content readContent = mapper.map(content);

        assertThat(readContent.getId(), equalTo("http://www.ft.com/thing/" + uuid.toString()));
        assertThat(readContent.getTitle(), equalTo("Philosopher"));
        assertThat(readContent.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        assertThat(readContent.getType(), equalTo(TypeResolver.TYPE_IMAGE_SET));
        assertThat(readContent.getByline(), equalTo("David Jules"));
        assertThat(readContent.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        assertThat(readContent.getMembers().first(), equalTo(new Uri("http://localhost/content/" + memberUuid.toString())));
        assertThat(readContent.getPublishedDate(), equalTo(new DateTime(publishDate.getTime())));
        assertThat(readContent.getLastModified(), equalTo(new DateTime(lastModified.getTime())));
    }
}
