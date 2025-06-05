import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';

/**
 * WebSocket聊天服务
 */
class ChatWebSocketService {
  constructor() {
    this.ws = null;
    this.isConnected = ref(false);
    this.isConnecting = ref(false);
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 3000;
    this.messageHandlers = new Set();
    this.connectionHandlers = new Set();
  }

  /**
   * 连接WebSocket
   */
  connect(userId, sessionId = null) {
    if (this.isConnected.value || this.isConnecting.value) {
      return Promise.resolve();
    }

    return new Promise((resolve, reject) => {
      try {
        this.isConnecting.value = true;
        
        // 构建WebSocket URL
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const host = window.location.hostname;
        const port = import.meta.env.DEV ? '8080' : window.location.port;
        
        let url = `${protocol}//${host}:${port}/ws/chat?userId=${userId}`;
        if (sessionId) {
          url += `&sessionId=${sessionId}`;
        }

        this.ws = new WebSocket(url);

        this.ws.onopen = () => {
          console.log('WebSocket连接已建立');
          this.isConnected.value = true;
          this.isConnecting.value = false;
          this.reconnectAttempts = 0;
          
          // 通知连接处理器
          this.connectionHandlers.forEach(handler => {
            try {
              handler({ type: 'connected' });
            } catch (error) {
              console.error('连接处理器执行失败:', error);
            }
          });
          
          resolve();
        };

        this.ws.onmessage = (event) => {
          try {
            const message = JSON.parse(event.data);
            console.log('收到WebSocket消息:', message);
            
            // 通知消息处理器
            this.messageHandlers.forEach(handler => {
              try {
                handler(message);
              } catch (error) {
                console.error('消息处理器执行失败:', error);
              }
            });
          } catch (error) {
            console.error('解析WebSocket消息失败:', error);
          }
        };

        this.ws.onclose = (event) => {
          console.log('WebSocket连接已关闭:', event.code, event.reason);
          this.isConnected.value = false;
          this.isConnecting.value = false;
          
          // 通知连接处理器
          this.connectionHandlers.forEach(handler => {
            try {
              handler({ type: 'disconnected', code: event.code, reason: event.reason });
            } catch (error) {
              console.error('连接处理器执行失败:', error);
            }
          });
          
          // 自动重连
          if (event.code !== 1000 && this.reconnectAttempts < this.maxReconnectAttempts) {
            this.scheduleReconnect(userId, sessionId);
          }
        };

        this.ws.onerror = (error) => {
          console.error('WebSocket错误:', error);
          this.isConnecting.value = false;
          reject(error);
        };

      } catch (error) {
        this.isConnecting.value = false;
        reject(error);
      }
    });
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.ws) {
      this.ws.close(1000, '用户主动断开');
      this.ws = null;
    }
    this.isConnected.value = false;
    this.isConnecting.value = false;
    this.reconnectAttempts = 0;
  }

  /**
   * 发送消息
   */
  sendMessage(message) {
    if (!this.isConnected.value || !this.ws) {
      throw new Error('WebSocket未连接');
    }

    try {
      const messageData = {
        type: 'text',
        content: message,
        timestamp: new Date().toISOString()
      };
      
      this.ws.send(JSON.stringify(messageData));
      console.log('发送WebSocket消息:', messageData);
    } catch (error) {
      console.error('发送消息失败:', error);
      throw error;
    }
  }

  /**
   * 添加消息处理器
   */
  onMessage(handler) {
    this.messageHandlers.add(handler);
    
    // 返回取消订阅函数
    return () => {
      this.messageHandlers.delete(handler);
    };
  }

  /**
   * 添加连接状态处理器
   */
  onConnection(handler) {
    this.connectionHandlers.add(handler);
    
    // 返回取消订阅函数
    return () => {
      this.connectionHandlers.delete(handler);
    };
  }

  /**
   * 计划重连
   */
  scheduleReconnect(userId, sessionId) {
    this.reconnectAttempts++;
    console.log(`计划重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
    
    setTimeout(() => {
      if (!this.isConnected.value) {
        console.log('尝试重连...');
        this.connect(userId, sessionId).catch(error => {
          console.error('重连失败:', error);
        });
      }
    }, this.reconnectInterval);
  }

  /**
   * 获取连接状态
   */
  getConnectionState() {
    return {
      isConnected: this.isConnected,
      isConnecting: this.isConnecting
    };
  }
}

// 创建全局实例
export const chatWebSocket = new ChatWebSocketService();

// 导出服务类
export default ChatWebSocketService;