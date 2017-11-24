import zlib

import tornado.ioloop  
import tornado.web  


class MainHandler(tornado.web.RequestHandler):  
    def get(self):  
        self.write("Hello, world")
    
    def post(self, *args, **kwargs):
        print self.request.uri
        if self.request.headers and self.request.headers.get('Content-Encoding') == 'deflate':
            print self.request.headers.get('Content-Encoding')
            print zlib.decompress(self.request.body)
        else :
            print self.request.body
        
application = tornado.web.Application([  
    #data_monitor_agent_api
    (r"/intake/?", MainHandler),
    (r"/intake/ping?", MainHandler),
    (r"/intake/metrics?", MainHandler),
    (r"/intake/metadata?", MainHandler),
    (r"/api/v1/series/?", MainHandler),
    (r"/api/v1/check_run/?", MainHandler),
    (r"/status/?", MainHandler), 
])  
  
if __name__=="__main__":  
    application.listen(8888)  
    tornado.ioloop.IOLoop.instance().start()  
