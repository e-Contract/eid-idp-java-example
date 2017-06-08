/*
 * eID Identity Provider Java Example.
 *
 * Copyright 2017 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.idp.example;

import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;

@WebServlet("/photo.jpg")
public class PhotoServlet extends HttpServlet {

    @Inject
    private ExampleController exampleController;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Attribute> attributes = this.exampleController.getAttributes();
        for (Attribute attribute : attributes) {
            if (attribute.getName().equals("http://axschema.org/eid/photo")) {
                String encodedPhoto = attribute.getValue();
                byte[] photo = Base64.decodeBase64(encodedPhoto);
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=-1"); // http 1.1											// 1.1
                response.setHeader("Pragma", "no-cache, no-store"); // http 1.0
                response.setDateHeader("Expires", -1);
                response.setContentType("image/jpeg");
                ServletOutputStream out = response.getOutputStream();
                out.write(photo);
                out.close();
            }
        }
    }
}
