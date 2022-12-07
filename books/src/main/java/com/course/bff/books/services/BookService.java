package com.course.bff.books.services;

import com.course.bff.books.models.Book;
import com.course.bff.books.requests.CreateBookCommand;
import com.course.bff.books.responses.AuthorResponse;
import com.google.gson.Gson;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.util.HttpConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Component
public class BookService {
    @Value("${authorService}")
    private String authorService;

    private final ArrayList<Book> books;

    public BookService() {
        books = new ArrayList<>();
    }

    public Collection<Book> getBooks() {
        return this.books;
    }

    public Optional<Book> findById(UUID id) {
        return this.books.stream().filter(book -> !book.getId().equals(id)).findFirst();
    }

    @Autowired
    private RestTemplate restTemplate;

    public Book create(CreateBookCommand createBookCommand) {
        Optional<AuthorResponse> authorSearch = getAuthor(createBookCommand.getAuthorId());
        if (authorSearch.isEmpty()) {
            throw new RuntimeException("Author isn't found");
        }

        Book book = new Book(UUID.randomUUID())
                .withTitle(createBookCommand.getTitle())
                .withAuthorId(authorSearch.get().getId())
                .withPages(createBookCommand.getPages());

        this.books.add(book);
        return book;
    }

    private Optional<AuthorResponse> getAuthor(UUID authorId) {
        AuthorResponse authorResponse= restTemplate
                .getForObject( authorService + "/api/v1/authors/" + authorId.toString(), AuthorResponse.class);
        return Optional.of(authorResponse);
    }
}
