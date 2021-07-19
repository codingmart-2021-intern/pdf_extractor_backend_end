package com.cm.pdfextractor.pdfextractor.service;

import com.cm.pdfextractor.pdfextractor.model.User;
import org.codehaus.jettison.json.JSONException;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {

    ResponseEntity<User> findById(Long id);

    User findByEmail(String email);

    List<User> findAll();

    ResponseEntity<?> addData(User user) throws Exception;

    ResponseEntity<?> updateData(User user, Long id) throws JSONException;

    ResponseEntity<String> deleteData(Long id) throws JSONException;

    String returnJsonString(boolean status, String response) throws JSONException;
}
