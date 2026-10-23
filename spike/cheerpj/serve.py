#!/usr/bin/env python3
"""Static file server WITH HTTP Range support, for the CheerpJ spike.

CheerpJ streams the runtime and jars using HTTP Range requests and refuses to
run against a server that does not support them ("HTTP server does not support
the 'Range' header. CheerpJ cannot run."). Python's stdlib
`python3 -m http.server` does NOT support Range, so this small wrapper adds it.

Usage:  python3 serve.py [port]   (default 8111)
"""
import http.server
import os
import re
import socketserver
import sys

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 8111


class RangeRequestHandler(http.server.SimpleHTTPRequestHandler):
    """SimpleHTTPRequestHandler that honours single-range byte requests."""

    def send_head(self):
        rng = self.headers.get("Range")
        if rng is None:
            return super().send_head()

        m = re.match(r"bytes=(\d*)-(\d*)", rng)
        if not m:
            return super().send_head()

        path = self.translate_path(self.path)
        if not os.path.isfile(path):
            return super().send_head()

        size = os.path.getsize(path)
        start = int(m.group(1)) if m.group(1) else 0
        end = int(m.group(2)) if m.group(2) else size - 1
        end = min(end, size - 1)
        if start > end:
            self.send_error(416, "Requested Range Not Satisfiable")
            return None

        length = end - start + 1
        f = open(path, "rb")
        f.seek(start)
        self.send_response(206)
        ctype = self.guess_type(path)
        self.send_header("Content-Type", ctype)
        self.send_header("Accept-Ranges", "bytes")
        self.send_header("Content-Range", f"bytes {start}-{end}/{size}")
        self.send_header("Content-Length", str(length))
        self.end_headers()
        self._range_remaining = length
        return f

    def copyfile(self, source, outputfile):
        remaining = getattr(self, "_range_remaining", None)
        if remaining is None:
            return super().copyfile(source, outputfile)
        while remaining > 0:
            chunk = source.read(min(64 * 1024, remaining))
            if not chunk:
                break
            outputfile.write(chunk)
            remaining -= len(chunk)
        self._range_remaining = None

    def end_headers(self):
        # Harmless CORS/caching niceties for local testing.
        self.send_header("Accept-Ranges", "bytes")
        super().end_headers()


class ThreadingServer(socketserver.ThreadingMixIn, http.server.HTTPServer):
    daemon_threads = True


if __name__ == "__main__":
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    with ThreadingServer(("", PORT), RangeRequestHandler) as httpd:
        print(f"Serving {os.getcwd()} with Range support on http://localhost:{PORT}")
        httpd.serve_forever()
