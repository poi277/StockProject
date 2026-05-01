'use client';
import { useState } from 'react';
import { AuthProvider } from "../context/AuthContext"
import { WebSocketProvider } from "../util/WebSocketContext"
import './globals.css'
import '../tossCss/toss-layout.css'
import { OrderProvider } from '../util/OrderSocket';
import { UserWebSocketProvider } from '../util/UserWebSocketContext';

export default function RootLayout({ children }) {
  return (<html  lang="ko"    data-theme="dark" data-wts-theme="dark" className="tw3v-n7og3x0"style={{ colorScheme: 'dark' }} >
      <body 
        className="tw3v-n7og3x0 _150j7tw0" 
        style={{ 
          "--wts-body-background-color": "#101013",
        }}
        // 사진(image_ce8024.png)에 있는 추가 속성까지 맞춘다면:
        data-tabster='{"root":{}}'
      >
        <AuthProvider>
          <WebSocketProvider>
            <UserWebSocketProvider> 
             <OrderProvider>
               {children}
            </OrderProvider>
            </UserWebSocketProvider> 
          </WebSocketProvider>
        </AuthProvider>
      </body>
    </html>
  );
}