'use client';
import { useState } from 'react';
import { AuthProvider } from "../context/AuthContext"
import { WebSocketProvider } from "../util/WebSocket"
export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>
        <AuthProvider>
          <WebSocketProvider>
          {children}
        </WebSocketProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
