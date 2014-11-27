import junit.framework.*;
import com.mockobjects.servlet.*;

public class TestTestingLabConverterServlet extends TestCase {

  public void test_bad_parameter() throws Exception {
    TestingLabConverterServlet s = new TestingLabConverterServlet();
    MockHttpServletRequest request = 
      new MockHttpServletRequest();
    MockHttpServletResponse response = 
      new MockHttpServletResponse();
    
    request.setupAddParameter("farenheitTemperature", "boo!");
    response.setExpectedContentType("text/html");
    s.doGet(request,response);
    response.verify();
      System.out.println(response.getOutputStreamContents());
      assertEquals("<html><head><title>Bad Temperature</title>"
                   + "</head><body><h2>Need to enter a valid temperature!"
                   + "Got a NumberFormatException on "
                   + "boo!"
                   + "</h2></body></html>\n"
                   ,
                 response.getOutputStreamContents());
  }
    
    public void test_format() throws Exception {
        TestingLabConverterServlet s = new TestingLabConverterServlet();
        MockHttpServletRequest request =
        new MockHttpServletRequest();
        MockHttpServletResponse response =
        new MockHttpServletResponse();
        
        request.setupAddParameter("farenheitTemperature", "2.12e2");
        response.setExpectedContentType("text/html");
        s.doGet(request,response);
        response.verify();
        System.out.println(response.getOutputStreamContents());
        assertEquals("<html><head><title>Temperature Converter Result</title>"
                     + "</head><body><h2>" + "2.12e2" + " Farenheit = " + "100" + " Celsius "
                     + "</h2>\n"
                     +"<p><h3>The temperature in Austin is " + "451" + " degrees Farenheit</h3>\n"
                     +"</body></html>\n"
                     ,
                     response.getOutputStreamContents());
    }
    
    public void test_precision() throws Exception {
        TestingLabConverterServlet s = new TestingLabConverterServlet();
        MockHttpServletRequest request =
        new MockHttpServletRequest();
        MockHttpServletResponse response =
        new MockHttpServletResponse();
        
        request.setupAddParameter("farenheitTemperature", "211.99191919");
        response.setExpectedContentType("text/html");
        s.doGet(request,response);
        response.verify();
        System.out.println(response.getOutputStreamContents());
        assertEquals("<html><head><title>Temperature Converter Result</title>"
                     + "</head><body><h2>" + "211.99191919" + " Farenheit = " + "100" + " Celsius "
                     + "</h2>\n"
                     +"<p><h3>The temperature in Austin is " + "451" + " degrees Farenheit</h3>\n"
                     +"</body></html>\n"
                     ,
                     response.getOutputStreamContents());
        
        s = new TestingLabConverterServlet();
        request =new MockHttpServletRequest();
        response =new MockHttpServletResponse();
        
        request.setupAddParameter("farenheitTemperature", "123.456");
        response.setExpectedContentType("text/html");
        s.doGet(request,response);
        response.verify();
        System.out.println(response.getOutputStreamContents());
        assertEquals("<html><head><title>Temperature Converter Result</title>"
                     + "</head><body><h2>" + "123.456" + " Farenheit = " + "50.81" + " Celsius "
                     + "</h2>\n"
                     +"<p><h3>The temperature in Austin is " + "451" + " degrees Farenheit</h3>\n"
                     +"</body></html>\n"
                     ,
                     response.getOutputStreamContents());
    }
    
    public void test_range() throws Exception {
        TestingLabConverterServlet s = new TestingLabConverterServlet();
        MockHttpServletRequest request =
        new MockHttpServletRequest();
        MockHttpServletResponse response =
        new MockHttpServletResponse();
        
        request.setupAddParameter("farenheitTemperature", "300");
        response.setExpectedContentType("text/html");
        s.doGet(request,response);
        response.verify();
        System.out.println(response.getOutputStreamContents());
        assertEquals("<html><head><title>Temperature Converter Result</title>"
                     + "</head><body><h2>" + "300" + " Farenheit = " + "148.89" + " Celsius "
                     + "</h2>\n"
                     +"<p><h3>The temperature in Austin is " + "451" + " degrees Farenheit</h3>\n"
                     +"</body></html>\n"
                     ,
                     response.getOutputStreamContents());
        
        s = new TestingLabConverterServlet();
        request =new MockHttpServletRequest();
        response =new MockHttpServletResponse();
        
        request.setupAddParameter("farenheitTemperature", "-456");
        response.setExpectedContentType("text/html");
        s.doGet(request,response);
        response.verify();
        System.out.println(response.getOutputStreamContents());
        assertEquals("<html><head><title>Temperature Converter Result</title>"
                     + "</head><body><h2>" + "-456" + " Farenheit = " + "-271.11" + " Celsius "
                     + "</h2>\n"
                     +"<p><h3>The temperature in Austin is " + "451" + " degrees Farenheit</h3>\n"
                     +"</body></html>\n"
                     ,
                     response.getOutputStreamContents());
        
        s = new TestingLabConverterServlet();
        request =new MockHttpServletRequest();
        response =new MockHttpServletResponse();
        
        request.setupAddParameter("farenheitTemperature", "0");
        response.setExpectedContentType("text/html");
        s.doGet(request,response);
        response.verify();
        System.out.println(response.getOutputStreamContents());
        assertEquals("<html><head><title>Temperature Converter Result</title>"
                     + "</head><body><h2>" + "0" + " Farenheit = " + "-17.78" + " Celsius "
                     + "</h2>\n"
                     +"<p><h3>The temperature in Austin is " + "451" + " degrees Farenheit</h3>\n"
                     +"</body></html>\n"
                     ,
                     response.getOutputStreamContents());
    }
    
    
  
  public void test_boil() throws Exception {
    TestingLabConverterServlet s = new TestingLabConverterServlet();
    MockHttpServletRequest request = 
      new MockHttpServletRequest();
    MockHttpServletResponse response = 
      new MockHttpServletResponse();
    
    request.setupAddParameter("farenheitTemperature", "212");
    response.setExpectedContentType("text/html");
    s.doGet(request,response);
    response.verify();
      System.out.println(response.getOutputStreamContents());
      assertEquals("<html><head><title>Temperature Converter Result</title>"
                   + "</head><body><h2>" + "212" + " Farenheit = " + "100" + " Celsius "
                   + "</h2>\n"
                   +"<p><h3>The temperature in Austin is " + "451" + " degrees Farenheit</h3>\n"
                   +"</body></html>\n",
                 response.getOutputStreamContents());
  }
    
  
}

