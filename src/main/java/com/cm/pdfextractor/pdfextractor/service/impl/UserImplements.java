package com.cm.pdfextractor.pdfextractor.service.impl;

import com.cm.pdfextractor.pdfextractor.model.User;
import com.cm.pdfextractor.pdfextractor.repository.UserRepository;
import com.cm.pdfextractor.pdfextractor.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserImplements implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // find user by d
    @Override
    public ResponseEntity<User> findById(Long id) {
        User res_data = userRepository.findById(id).get();
        if (res_data != null) {
            return new ResponseEntity<>(res_data, HttpStatus.OK);
        }
        return new ResponseEntity<>((User) res_data, HttpStatus.NOT_FOUND);
    }

    @Override
    public User findByEmail(String email) {
        User fetchUser = userRepository.findByEmail(email);
        return fetchUser;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // signup
    @Override
    public ResponseEntity<?> addData(User user) throws Exception {
        System.out.println("user = " + objectMapper.writeValueAsString(user));
        User existData = userRepository.findByEmail(user.getEmail());

        if (existData != null) {
            return new ResponseEntity<>(returnJsonString(false, "Email already exist please try with new mail"),
                    HttpStatus.FORBIDDEN);
        } else {
            user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
            userRepository.save(user);
            return new ResponseEntity<>(returnJsonString(true, "SignIn success"),
                    HttpStatus.CREATED);
        }
    }

    // update user profile
    @Override
    public ResponseEntity<?> updateData(User user, Long id) throws JSONException {
        User exist = userRepository.findById(id).orElse(null);
        if (exist != null) {
            exist.setName(user.getName());
            exist.setPassword(exist.getPassword());
            return new ResponseEntity<>(userRepository.save(exist), HttpStatus.OK);
        }
        return new ResponseEntity<>(returnJsonString(false, "user Not found"), HttpStatus.OK);
    }

    // delete the data
    @Override
    public ResponseEntity<String> deleteData(Long id) throws JSONException {
        try {
            userRepository.deleteById(id);
            return new ResponseEntity<String>(returnJsonString(true, "User deleted successfully"), HttpStatus.OK);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<String>(returnJsonString(false, "User deletion failed"), HttpStatus.OK);
    }

    @Override
    public String returnJsonString(boolean status, String response) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", status);
        jsonObject.put("message", response);
        return jsonObject.toString();
    }

}
