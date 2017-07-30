/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package example.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import example.app.service.CalculatorService;

/**
 * The SimpleCachingExampleApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@Controller
@SuppressWarnings("unused")
public class SimpleCachingExampleApplication {

  private static final String RESPONSE = "<h1>%s</h1>";

  public static void main(String[] args) {
    SpringApplication.run(SimpleCachingExampleApplication.class, args);
  }

  @Autowired
  private CalculatorService calculatorService;

  @RequestMapping(method = RequestMethod.GET, value = "/factorial/{number}")
  @ResponseBody
  public String computeFactorial(@PathVariable("number") Long number) {
    return String.format(RESPONSE, (number != null ? calculatorService.factorial(number) : "?"));
  }
}
