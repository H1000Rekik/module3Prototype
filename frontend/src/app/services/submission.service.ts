import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval, switchMap, takeWhile, tap } from 'rxjs';

export interface SubmissionRequest {
  username: string;
  code: string;
  mode: 'run' | 'submit';
}

export interface Submission {
  id: number;
  username: string;
  code: string;
  language: string;
  mode: string;
  verdict: string;
  runtime: number;
  memory: number;
  output: string;
  errorOutput: string;
  status: 'PENDING' | 'PROCESSING' | 'DONE';
  submittedAt: string;
}

@Injectable({ providedIn: 'root' })
export class SubmissionService {
  private api = 'http://localhost:8087/api/submissions';

  constructor(private http: HttpClient) {}

  submit(req: SubmissionRequest): Observable<{ id: number; status: string }> {
    return this.http.post<{ id: number; status: string }>(this.api, req);
  }

  getById(id: number): Observable<Submission> {
    return this.http.get<Submission>(`${this.api}/${id}`);
  }

  getUserHistory(username: string): Observable<Submission[]> {
    return this.http.get<Submission[]>(`${this.api}/user/${username}`);
  }

  /**
   * Polls every 1.5s until status is DONE
   */
  pollUntilDone(id: number): Observable<Submission> {
    return interval(1500).pipe(
      switchMap(() => this.getById(id)),
      takeWhile(s => s.status !== 'DONE', true)
    );
  }
}
