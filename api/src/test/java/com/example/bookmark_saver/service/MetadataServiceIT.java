package com.example.bookmark_saver.service;

import com.example.bookmark_saver.domain.Metadata;
import com.example.bookmark_saver.repository.BookmarkRepository;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MetadataServiceIT {
    /**
     * WireMock server started on a random port for each test.
     */
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    /**
     * Bookmark repository mock — required to instantiate the service,
     * but not involved in extraction tests.
     */
    @Mock
    private BookmarkRepository repository;

    /**
     * Returns the base URL of the WireMock server.
     *
     * @return The WireMock base URL.
     */
    private String wireMockUrl() {
        return "http://localhost:" + wireMock.getPort();
    }

    /**
     * Stubs a GET request to WireMock returning the given HTML body.
     *
     * @param html The HTML body to return.
     */
    private void stubHtml(String html) {
        wireMock
            .stubFor(
                get(anyUrl())
                    .willReturn(aResponse()
                    .withHeader("Content-Type", "text/html")
                    .withBody(html)
                )
            );
    }

    // ---------------------------------------------------------------
    // extract
    // ---------------------------------------------------------------

    @Test
    void extractParsesOgTitle() {
        stubHtml("""
            <html>
                <head>
                    <meta property="og:title" content="Open Graph Title" />
                </head>
            </html>
        """);

        MetadataService service = new MetadataService(repository);
        Metadata result = service.extract(wireMockUrl());

        assertThat(result.getTitle())
            .isEqualTo("Open Graph Title");
    }

    @Test
    void extractFallsBackToDocumentTitleWhenOgTitleIsMissing() {
        stubHtml("""
            <html>
                <head>
                    <title>Document Title</title>
                </head>
            </html>
        """);

        MetadataService service = new MetadataService(repository);
        Metadata result = service.extract(wireMockUrl());

        assertThat(result.getTitle())
            .isEqualTo("Document Title");
    }

    @Test
    void extractParsesOgDescription() {
        stubHtml("""
            <html>
                <head>
                    <meta property="og:description" content="Description longer than 20 characters." />
                </head>
            </html>
        """);

        MetadataService service = new MetadataService(repository);
        Metadata result = service.extract(wireMockUrl());

        assertThat(result.getDescription())
            .isEqualTo("Description longer than 20 characters.");
    }

    @Test
    void extractFallsBackToFirstParagraphWhenDescriptionIsTooShort() {
        stubHtml("""
            <html>
                <head>
                    <meta name="description" content="short" />
                </head>
                <body>
                    <article>
                        <p>This paragraph will be used as description.</p>
                    </article>
                </body>
            </html>
        """);

        MetadataService service = new MetadataService(repository);
        Metadata result = service.extract(wireMockUrl());

        assertThat(result.getDescription())
            .isEqualTo("This paragraph will be used as description.");
    }

    @Test
    void extractReturnsEmptyMetadataWhenPageIsUnreachable() {
        MetadataService service = new MetadataService(repository);
        Metadata result = service.extract("http://localhost:0");

        assertThat(result.getTitle())
            .isNull();

        assertThat(result.getDescription())
            .isNull();
    }

    @Test
    void extractParsesTwitterDescriptionWhenOgDescriptionIsMissing() {
        stubHtml("""
            <html>
                <head>
                    <meta name="twitter:description" content="Twitter description long enough to be valid" />
                </head>
            </html>
        """);

        MetadataService service = new MetadataService(repository);
        Metadata result = service.extract(wireMockUrl());

        assertThat(result.getDescription())
            .isEqualTo("Twitter description long enough to be valid");
    }

    @Test
    void extractParsesOgImage() {
        stubHtml("""
            <html>
                <head>
                    <meta property="og:image" content="https://example.com/image.jpg" />
                </head>
            </html>
        """);

        MetadataService service = new MetadataService(repository);
        Metadata result = service.extract(wireMockUrl());

        assertThat(result.getImageUrl())
            .isEqualTo("https://example.com/image.jpg");
    }

    @Test
    void extractFallsBackToTwitterImageWhenOgImageIsMissing() {
        stubHtml("""
            <html>
                <head>
                    <meta name="twitter:image" content="https://example.com/twitter.jpg" />
                </head>
            </html>
        """);

        MetadataService service = new MetadataService(repository);
        Metadata result = service.extract(wireMockUrl());

        assertThat(result.getImageUrl())
            .isEqualTo("https://example.com/twitter.jpg");
    }

    @Test
    void extractParsesFavicon() {
        stubHtml("""
            <html>
                <head>
                    <link rel="icon" href="https://example.com/favicon.ico" />
                </head>
            </html>
        """);

        MetadataService service = new MetadataService(repository);
        Metadata result = service.extract(wireMockUrl());

        assertThat(result.getFavicon())
            .isEqualTo("https://example.com/favicon.ico");
    }

    @Test
    void extractParsesSiteName() {
        stubHtml("""
            <html>
                <head>
                    <meta property="og:site_name" content="Example Site" />
                </head>
            </html>
        """);

        MetadataService service = new MetadataService(repository);
        Metadata result = service.extract(wireMockUrl());

        assertThat(result.getSiteName())
            .isEqualTo("Example Site");
    }

    @Test
    void extractExtractsDomainFromUrl() {
        stubHtml("<html></html>");

        MetadataService service = new MetadataService(repository);
        Metadata result = service.extract(wireMockUrl());

        assertThat(result.getDomain())
            .isEqualTo("localhost");
    }
}