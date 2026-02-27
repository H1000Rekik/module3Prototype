import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Client, Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-match',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './match.component.html',
  styleUrls: ['./match.component.css']
})
export class MatchComponent implements OnInit, OnDestroy {
  roomId: string = '';
  username: string = '';
  stompClient!: Client;
  room: any = null;
  code: string = '';
  language: string = 'java';

  // Chess Timer Logic
  timeRemaining: number = 600; // 10 minutes in seconds
  timerInterval: any;
  banInput: string = 'python'; // default ban to propose

  // To avoid TypeScript issues if sockjs doesn't export properly, we ensure we use it as constructor.

  constructor(private route: ActivatedRoute, private router: Router) { }

  ngOnInit() {
    this.username = sessionStorage.getItem('username') || '';
    if (!this.username) {
      this.router.navigate(['/auth']);
      return;
    }

    this.route.paramMap.subscribe(params => {
      this.roomId = params.get('roomId') || '';
      this.connectWebSocket();
    });
  }

  ngOnDestroy() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
    if (this.timerInterval) clearInterval(this.timerInterval);
  }

  connectWebSocket() {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8087/ws-match'),
      onConnect: (frame) => {
        console.log('Connected: ' + frame);
        this.stompClient.subscribe('/topic/match/' + this.roomId, (message) => {
          this.room = JSON.parse(message.body);
          this.checkStateChange();
        });

        // Send JOIN event
        this.stompClient.publish({
          destination: `/app/match/${this.roomId}/join`,
          body: JSON.stringify({ username: this.username })
        });
      }
    });
    this.stompClient.activate();
  }

  checkStateChange() {
    if (this.room.status === 'CODING' && !this.timerInterval) {
      this.timerInterval = setInterval(() => {
        const elapsed = Math.floor((Date.now() - this.room.startTime) / 1000);
        this.timeRemaining = 600 - elapsed;
        if (this.timeRemaining <= 0) {
          this.timeRemaining = 0;
          clearInterval(this.timerInterval);
          // Timed out logic...
        }
      }, 1000);
    }
  }

  sendBan() {
    this.stompClient.publish({
      destination: `/app/match/${this.roomId}/ban`,
      body: JSON.stringify({ language: this.banInput })
    });
  }

  submitCode(mockResult: boolean) {
    // mockResult is used for testing. usually we'd call backend code execution and then send result.
    this.stompClient.publish({
      destination: `/app/match/${this.roomId}/submit`,
      body: JSON.stringify({
        username: this.username,
        code: this.code,
        language: this.language,
        isAccepted: mockResult
      })
    });
  }

  get formattedTime() {
    const m = Math.floor(this.timeRemaining / 60);
    const s = this.timeRemaining % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  }
}
