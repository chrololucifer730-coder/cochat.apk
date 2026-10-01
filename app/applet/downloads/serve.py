import os
import sys
from http.server import HTTPServer, SimpleHTTPRequestHandler
from socketserver import ThreadingMixIn

class ThreadingHTTPServer(ThreadingMixIn, HTTPServer):
    daemon_threads = True
    allow_reuse_address = True

class RobustRequestHandler(SimpleHTTPRequestHandler):
    def end_headers(self):
        if self.path.endswith('.aab') or self.path.endswith('.apk'):
            filename = os.path.basename(self.path.split('?')[0])
            self.send_header('Content-Disposition', f'attachment; filename="{filename}"')
            self.send_header('Content-Type', 'application/octet-stream')
        self.send_header('Cache-Control', 'no-cache, no-store, must-revalidate')
        super().end_headers()

    def log_message(self, format, *args):
        pass

if __name__ == '__main__':
    downloads_dir = '/app/applet/downloads'
    os.chdir(downloads_dir)
    server = ThreadingHTTPServer(('0.0.0.0', 3000), RobustRequestHandler)
    print("Serving on port 3000")
    server.serve_forever()
