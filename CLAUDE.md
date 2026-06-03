# watchface

## 프로젝트 개요
kakajeje/watchface 저장소. 개발 브랜치: `claude/agentmemory-cli-YiDls`

## 설치된 플러그인 (claude-plugins-official)

### 코드 품질
| 플러그인 | 사용 시점 |
|---|---|
| `code-review` | PR 전 코드 리뷰, `/code-review` |
| `code-simplifier` | 복잡한 코드 단순화, `/simplify` |
| `code-modernization` | 레거시 코드 현대화 |
| `security-guidance` | 보안 취약점 검토 |
| `typescript-lsp` | TypeScript 타입 오류, 자동완성 |
| `rust-analyzer-lsp` | Rust 코드 분석 |
| `jdtls-lsp` | Java 코드 분석 |

### Git / GitHub
| 플러그인 | 사용 시점 |
|---|---|
| `commit-commands` | 커밋 메시지 작성, 스테이징 |
| `github` | PR 생성, 이슈 관리, 브랜치 작업 |
| `feature-dev` | 새 기능 개발 시 워크플로우 |

### 프론트엔드 / UI
| 플러그인 | 사용 시점 |
|---|---|
| `frontend-design` | UI 컴포넌트 설계, 스타일링 |
| `figma` | Figma 디자인 연동 |
| `playwright` | E2E 테스트 자동화 |
| `chrome-devtools-mcp` | 브라우저 디버깅 |

### 백엔드 / 인프라
| 플러그인 | 사용 시점 |
|---|---|
| `supabase` | DB 스키마, 쿼리, Auth |
| `vercel` | 배포, 환경변수, 도메인 |
| `stripe` | 결제 연동 |
| `sentry` | 에러 모니터링 |
| `fastly-agent-toolkit` | CDN, 엣지 설정 |
| `postman` | API 테스트, 컬렉션 관리 |

### AI / 에이전트 개발
| 플러그인 | 사용 시점 |
|---|---|
| `agent-sdk-dev` | Claude Agent SDK 앱 개발 |
| `mcp-server-dev` | MCP 서버 개발 |
| `atomic-agents` | 에이전트 구성 |
| `playground` | 빠른 프로토타이핑 |
| `skill-creator` | 새 스킬 작성 |

### 문서 / 컨텍스트
| 플러그인 | 사용 시점 |
|---|---|
| `remember` | 세션 간 컨텍스트 유지, `/remember` |
| `claude-md-management` | CLAUDE.md 업데이트 |
| `claude-code-setup` | 프로젝트 초기 셋업 |
| `context7` | 라이브러리 최신 문서 조회 |
| `microsoft-docs` | MS 관련 문서 참조 |
| `sourcegraph` | 코드베이스 검색 |

### 기타
| 플러그인 | 사용 시점 |
|---|---|
| `ralph-loop` | 반복 작업 자동화 |
| `desktop-commander` | 로컬 시스템 명령 |
| `postiz` | 소셜 미디어 관리 |
| `nimble` | CRM 연동 |
| `circleback` | 미팅 노트 |
| `fakechat` | UI 목업용 채팅 데이터 |

## agentmemory
- 실행: `npx @agentmemory/agentmemory`
- REST API: `http://localhost:3111`
- Viewer: `http://localhost:3113`
- `data/` 디렉토리는 .gitignore로 제외됨

## 개발 규칙
- 항상 `claude/agentmemory-cli-YiDls` 브랜치에서 작업
- 커밋 후 `git push -u origin claude/agentmemory-cli-YiDls`
