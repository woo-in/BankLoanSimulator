# 🏦 우인은행 (Woo-in Bank)
> **Spring Boot 기반의 차세대 코어 뱅킹 시스템 시뮬레이션 프로젝트**

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Data JPA](https://img.shields.io/badge/JPA-Hibernate-59666C?style=flat-square&logo=hibernate)](https://spring.io/projects/spring-data-jpa)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-00758F?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-View-005F0F?style=flat-square&logo=thymeleaf)](https://www.thymeleaf.org/)

## 📝 프로젝트 소개 (Project Overview)
**우인은행**은 실제 금융권의 비즈니스 로직을 모방하여 설계된 웹 애플리케이션입니다.
단순한 입출금을 넘어, **대출 상품의 생애 주기(Life Cycle)** 전반을 관리하는 시스템을 구현하는 데 초점을 맞추었습니다.
고객의 신용 정보를 바탕으로 한도와 금리를 산출하고(Origination), 심사(Underwriting)를 거쳐 대출을 실행(Execute)하며, 사후 관리(Servicing)까지 이어지는 금융 프로세스를 경험할 수 있습니다.

---

## 🏗️ 시스템 아키텍처 및 모듈 (Modules)
본 프로젝트는 도메인 주도 설계(DDD)를 지향하며, 각 기능별로 명확한 역할을 분담하고 있습니다.

### 1. 👤 회원 및 인증 (Member)
* **패키지:** `bankapp.member`
* 고객 회원가입 및 로그인 처리
* Spring Security 기반의 인증/인가 관리

### 2. 💳 수신 및 계좌 (Account)
* **패키지:** `bankapp.account`
* 입출금 계좌 개설 프로세스
* 계좌 간 송금 및 거래 내역(Transaction) 기록
* 동시성 제어를 고려한 잔액 관리

### 3. 💰 여신 (Loan) - Core Features
대출 프로세스는 상품의 성격과 진행 단계에 따라 세분화되어 관리됩니다.

| 모듈명 | 설명 | 주요 기능 |
| --- | --- | --- |
| **Product** | 상품 관리 | 여신 상품 등록/수정, 금리 및 한도 정책 설정, 상품 판매 상태 관리 |
| **Origination** | 대출 신청 | 고객 재무 정보 수집(자산/소득/부채), **DSR(총부채원리금상환비율) 계산**, 가심사 및 한도 조회 |
| **Underwriting** | 대출 심사 | 신청된 대출 건에 대한 승인/거절 심사 프로세스 |
| **Execute** | 대출 실행 | 대출 약정 체결 및 실제 대출금 입금(Disbursement) 처리 |
| **Servicing** | 사후 관리 | 원리금 상환, 연체 관리, 이자 납입 등 대출 실행 이후의 관리 |

---

## 📸 주요 기능 미리보기 (Screenshots)

| 대출 한도 조회 | 타행 대출 정보 입력 |
| :---: | :---: |
| <img src="이미지주소_또는_파일경로1" width="400"> | <img src="이미지주소_또는_파일경로2" width="400"> |
> *스트레스 DSR 규제를 반영하여 타행 대출 정보를 포함한 정교한 한도 산출 기능을 제공합니다.*

---

## 🛠️ 기술적 특징 (Key Technical Features)
* **정교한 금융 계산:** 원리금 균등, 원금 균등, 만기 일시 상환 등 다양한 상환 방식에 따른 이자 계산 로직 구현 (`AmortizationCalculator`)
* **확장성 있는 설계:** 대출 상품의 종류(신용/담보)와 금리 형태(변동/고정)에 따라 유연하게 대응 가능한 엔티티 구조
* **데이터 무결성:** 대출 실행 및 자금 이체 시 트랜잭션(`@Transactional`)을 통한 정합성 보장

---

## 🚀 시작하기 (Getting Started)

### Prerequisites
* Java 17+
* MySQL 8.0+

### Installation
1. Repository Clone
   ```bash
   git clone [https://github.com/woo-in/BankLoanSimulator.git](https://github.com/woo-in/BankLoanSimulator.git)