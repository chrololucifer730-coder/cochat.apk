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
    downloads_dir = '/app/applet/.downloads'
    if not os.path.exists(downloads_dir):
        os.makedirs(downloads_dir, exist_ok=True)
    os.chdir(downloads_dir)
    server = ThreadingHTTPServer(('0.0.0.0', 3000), RobustRequestHandler)
    print("Serving persistent downloads on port 3000")
    try:
        server.serve_forever()
    except Exception:
        sys.exit(0)
