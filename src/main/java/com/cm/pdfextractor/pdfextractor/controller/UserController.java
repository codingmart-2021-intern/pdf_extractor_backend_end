package com.cm.pdfextractor.pdfextractor.controller;

import com.cm.pdfextractor.pdfextractor.config.jwt_configure.JwtTokenProvider;
import com.cm.pdfextractor.pdfextractor.model.User;
import com.cm.pdfextractor.pdfextractor.repository.UserRepository;
import com.cm.pdfextractor.pdfextractor.service.UserService;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserService userService;

    // Login AUTHENTICATE..
    @PostMapping(value = "/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody User user) throws Exception {
        log.info("UserResourceImpl : authenticate");

        JSONObject jsonObject = new JSONObject();
        User res_data = userService.findByEmail(user.getEmail());
        System.out.println(res_data.getEmail());
        if (res_data == null) {
            return new ResponseEntity<String>(
                    userService.returnJsonString(false, "your data is not found in database"),
                    HttpStatus.NOT_FOUND);
        }
        System.out.println(res_data.getEmail());
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        try {
            String email = user.getEmail();

            User user_data = userService.findByEmail(email);
            jsonObject.put("id", user_data.getUser_id());
            jsonObject.put("name", user_data.getName());
            jsonObject.put("email", user_data.getEmail());
            jsonObject.put("token", tokenProvider.createToken(email, user_data.getName()));
            System.out.println(res_data.getEmail());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.OK);
    }

    // signup
    @PostMapping(value = "/signup")
    public ResponseEntity<?> addData(@RequestBody User user) throws Exception {
        return userService.addData(user);
    }

    // get user details by id
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    /* Role Assign */

    // Change User details By users (profile page)
    @PutMapping(value = "/{id}")
    public ResponseEntity<?> updateData(@RequestBody User user, @PathVariable Long id) throws JSONException {
        return userService.updateData(user, id);
    }

    // get only user not admin details
    @GetMapping("/admin")
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    // deleting user details by admin
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteData(@PathVariable Long id) throws JSONException {
        ResponseEntity<User> user = userService.findById(id);

        return userService.deleteData(id);
    }

    // invalid exception
    @GetMapping("/invalid")
    public String invalid() {
        log.info("executing invalid");
        return "{'message', 'SOMETHING WENT WRONG'}";
    }
}
