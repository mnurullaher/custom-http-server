## Custom-HTTP-Server
***
&ensp;&ensp;&ensp;&ensp;This is a basic HTTP server which accepts HTTP requests in 
versions "1.1" and "1.0". Users can create end-points and 
read the properties of a coming request to this end-point 
as well as specify the properties of responses.
***
**Here is an example of usage with some example methods:**

```java
    var server = new HttpServer();
    server.handle("POST", "/path", (req, resp) -> {
        var requestBody = req.getBody();
        resp.setContent("content");
        resp.addHeader("New-Header", "This is new header");
        resp.removeHeader("New-Header");
    });
    server.start(8080);
    server.shutDown();
```
&ensp;&ensp;&ensp;&ensp;As can be seen in the example; properties of requests can
be read and properties of responses can be easily manipulated.
And the server can be started on a specific port and shut down
at any time.

