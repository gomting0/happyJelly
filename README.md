# 🐾 Happy Jelly 반려견 유치원 관리 시스템

<div align="center">
  <img src="https://path_to_your_logo.png" alt="Happy Jelly Logo" width="200"/>
  
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
  [![Oracle](https://img.shields.io/badge/Oracle-19c-red.svg)](https://www.oracle.com/database/)
  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
</div>

## 📚 목차

1. [프로젝트 소개](#-프로젝트-소개)
2. [주요 기능](#-주요-기능)
3. [기술 스택](#-기술-스택)
4. [데이터베이스 설계](#-데이터베이스-설계)
5. [설치 및 실행 방법](#-설치-및-실행-방법)
6. [팀 소개](#-팀-소개)
7. [라이선스](#-라이선스)

## 🐶 프로젝트 소개

Happy Jelly는 반려견 유치원을 위한 종합 관리 시스템입니다. 이 시스템은 유치원 운영에 필요한 다양한 기능을 제공하여 효율적인 관리와 고객 서비스 향상을 목표로 합니다. 반려견의 행복과 보호자의 만족을 최우선으로 생각하는 Happy Jelly와 함께 즐거운 유치원 생활을 경험해보세요!

## ✨ 주요 기능

1. **👥 회원 관리**
   - 사용자 등록, 로그인, 프로필 관리
   - 역할 기반 접근 제어 (RBAC)

2. **🐕 강아지 관리**
   - 강아지 정보 등록 및 관리
   - 강아지 프로필 조회

3. **📝 입학 신청**
   - 온라인 입학 신청 및 처리
   - 신청 상태 추적

4. **📊 출석 관리**
   - 강아지 출석 체크 및 기록
   - 출석 현황 조회

5. **📰 일일 리포트**
   - 강아지 활동 및 상태에 대한 일일 보고서 작성
   - 보고서 조회

6. **💉 백신 관리**
   - 강아지 백신 접종 기록 및 관리
   - 백신 증명서 업로드 및 조회

7. **💳 결제 시스템**
   - 카카오페이 연동을 통한 결제 처리
   - 결제 내역 조회

8. **🏢 지점 관리**
   - 여러 지점의 정보 관리 및 조회

9. **👨‍💼 직원 관리**
   - 직원 정보 관리 및 권한 설정

## 🛠 기술 스택

- **백엔드**: 
  - Spring Boot
  - Java
- **데이터베이스**: 
  - Oracle Database
- **개발 도구**: 
  - SQL Developer
- **프론트엔드**: 
  - HTML, CSS, JavaScript
  - Thymeleaf

## 📊 데이터베이스 설계

### 주요 테이블

1. **MEMBERS**
   - 회원 정보 저장
   - 칼럼: MEMBER_ID, USERNAME, PASSWORD, EMAIL, PHONE, ROLE

2. **DOGS**
   - 강아지 정보 저장
   - 칼럼: DOG_ID, MEMBER_ID, NAME, BREED, BIRTH_DATE, GENDER

3. **ADMISSIONS**
   - 입학 신청 정보 저장
   - 칼럼: ADMISSION_ID, DOG_ID, APPLICATION_DATE, STATUS

4. **ATTENDANCE**
   - 출석 정보 저장
   - 칼럼: ATTENDANCE_ID, DOG_ID, DATE, STATUS

### 샘플 쿼리

```sql
-- 회원 정보 조회
SELECT * FROM MEMBERS WHERE username = 'example_user';

-- 특정 강아지의 출석 기록 조회
SELECT * FROM ATTENDANCE 
WHERE dog_id = 1 
ORDER BY attendance_date DESC;
