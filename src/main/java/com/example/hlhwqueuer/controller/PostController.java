package com.example.hlhwqueuer.controller;

import com.amazonaws.services.sqs.AmazonSQS;
import com.example.hlhwqueuer.dto.PostCreateDto;
import com.example.hlhwqueuer.dto.QueuePostCreate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.jsonwebtoken.Jwe;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.JwtTokenizer;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.security.Key;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Slf4j
public class PostController {
    @Value("${key: Test}")
    private String key;
    @Value("${queue-url: queue}")
    private String queue;
    private final AmazonSQS amazonSQS;
    private final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    @PostMapping
    public ResponseEntity createPost(@RequestHeader("Authorization") final String bearer, @RequestBody PostCreateDto postCreateDto) throws JsonProcessingException {
        JwtParser jwtParser = Jwts.parser().verifyWith((SecretKey) getSigningKey()).build();
        try {
            var jwt = jwtParser.parse(bearer).accept(Jwt.UNSECURED_CLAIMS);
            var id = (String) jwt.getPayload().get("user_id");

            var message = new QueuePostCreate(id, postCreateDto.getContent());
            var json = objectWriter.writeValueAsString(message);
            amazonSQS.sendMessage(queue, json);
            return ResponseEntity.ok().build();
        }
        catch (Exception e){
            log.error("failed to process event: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
