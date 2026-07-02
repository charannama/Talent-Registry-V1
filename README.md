# Talent-Registry-V1
A Real World Issue Turned Into Trust based Platform  --Building Trustt between  Student And Enterprise.
# ZenCube Talent Registry (TR)

> **Enterprise Talent Verification & Hiring Coordination Platform**

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Status](https://img.shields.io/badge/status-DRAFT-orange)
![Backend](https://img.shields.io/badge/backend-Spring_Boot-green)
![Frontend](https://img.shields.io/badge/frontend-ReactJS-blue)
![Database](https://img.shields.io/badge/database-PostgreSQL-blue)
![License](https://img.shields.io/badge/license-Internal-red)

---

# Overview

ZenCube Talent Registry (TR) is an independent enterprise talent discovery platform built to bridge the gap between verified student talent and hiring organizations.

Unlike traditional job portals that rely on self-reported resumes, Talent Registry automatically generates verified student profiles directly from the ZenCube learning ecosystem using:

- Project execution history
- Rubric evaluations
- Mentor feedback
- Skill tags
- Academic information

The platform follows an **HR Intermediary Model**, ensuring that all enterprise-to-student communication is mediated by the ZenCube HR team to maintain privacy, governance, and recruitment quality.

---

# Project Objectives

The Talent Registry aims to:

- Provide verified student talent profiles
- Protect student privacy
- Simplify enterprise hiring
- Standardize recruitment workflows
- Enable transparent hiring pipelines
- Maintain complete auditability
- Support enterprise-grade governance

---

# Key Features

## Student Module

- OAuth2 Login via ZenCube
- Auto-generated Verified Profile
- Eligibility-aware Job Dashboard
- Application Tracking
- Opportunity Search
- Profile Synchronization
- Internship / Full-Time Eligibility Validation

---

## Enterprise Module

- Enterprise Registration
- HR Approval Workflow
- Job Posting
- Talent Search
- Express Interest
- Candidate Bookmarking
- Hiring Request Submission

---

## HR Module

- Candidate Review
- Hiring Pipeline Management
- Student Profile Moderation
- Interview Scheduling
- Enterprise Management
- Chat Moderation
- Calendar Coordination

---

## Admin Module

- User Management
- Role Management
- Feature Flags
- Analytics Dashboard
- Success Story Publishing
- Audit Monitoring
- System Configuration

---

# Business Principles

The system follows several mandatory architectural principles.

## Verified Profiles Only

Student profiles are generated exclusively from ZenCube project data.

Students cannot manually edit public profiles.

---

## HR Intermediary Model

Enterprises never communicate directly with students.

Every interaction is mediated through HR.

---

## Privacy First

Enterprise users SHALL NEVER access:

- Student Email
- Student Phone Number
- Private Academic Data

---

## Immutable Audit Trail

Every governed action generates an immutable audit log.

Examples include:

- Pipeline movement
- Profile suspension
- Enterprise approval
- Feature changes
- Role changes
- Configuration updates

---

# High-Level Architecture

```
                +----------------------+
                |      ZenCube         |
                | OAuth2 + REST APIs   |
                +----------+-----------+
                           |
                           |
                    Pull on Login
                           |
                           ▼
+--------------------------------------------------------+
|             Talent Registry Portal                     |
|                                                        |
| ReactJS Frontend                                       |
|                                                        |
| Spring Boot Backend                                    |
|                                                        |
| PostgreSQL Database                                    |
|                                                        |
| Notification Service                                   |
|                                                        |
| Scheduler                                               |
|                                                        |
| Audit Engine                                           |
|                                                        |
| Chat Module                                            |
|                                                        |
| Calendar Module                                        |
+--------------------------------------------------------+
```

---

# Technology Stack

## Frontend

- ReactJS
- JavaScript
- HTML5
- CSS3

---

## Backend

- Java 21
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate ORM

---

## Database

- PostgreSQL

---

## Authentication

- OAuth2
- JWT
- Role-Based Access Control (RBAC)

---

## Build Tools

- Maven

---

## Deployment

- Hostinger VPS
- Single Node Deployment

---

# System Modules

```
Authentication
│
├── OAuth2 Login
├── JWT Validation
└── RBAC

Students
│
├── Profile
├── Dashboard
├── Applications
└── Notifications

Enterprise
│
├── Registration
├── Job Openings
├── Candidate Search
└── Express Interest

HR
│
├── Hiring Pipeline
├── Calendar
├── Chat
├── Enterprise Review
└── Profile Moderation

Admin
│
├── Feature Flags
├── Analytics
├── Audit Logs
├── Role Management
└── System Configuration
```

---

# Hiring Pipeline

```
Applied
      │
      ▼
HR Reviewed
      │
      ▼
Forwarded
      │
      ▼
Interview Scheduled
      │
      ├────────► Rejected
      │
      ▼
Selected
```

---

# User Roles

| Role | Description |
|-------|-------------|
| Student | Browse opportunities and apply |
| Enterprise | Search verified talent |
| HR | Hiring workflow management |
| Admin | Platform governance |

---

# Project Structure

```
talent-registry/

│
├── frontend/
│
├── backend/
│
├── database/
│
├── documentation/
│
├── scripts/
│
├── docker/
│
├── postman/
│
├── diagrams/
│
├── README.md
│
└── LICENSE
```

---

# Backend Package Structure

```
com.zencube.tr

├── config
├── security
├── authentication
├── users
├── student
├── enterprise
├── hr
├── admin
├── opening
├── application
├── pipeline
├── notifications
├── calendar
├── chat
├── attachments
├── analytics
├── audit
├── featureflags
├── scheduler
├── integrations
├── exception
├── common
└── utils
```

---

# Database Highlights

The system uses PostgreSQL with UUID-based primary keys.

Major entities include:

- Users
- Roles
- Student Profiles
- Enterprises
- Openings
- Applications
- Hiring Pipeline
- Notifications
- Activities
- Chat
- Calendar
- Audit Logs
- Feature Flags
- Scheduled Tasks
- Attachments
- Analytics
- Sessions

---

# Security

The platform implements:

- OAuth2 Authentication
- JWT Authorization
- Role-Based Access Control
- Password Encryption
- Session Management
- Audit Logging
- Feature Flag Governance
- Secure File Validation

---

# Notifications

Supported notifications:

- In-App Notifications
- Email Notifications
- Calendar Invitations

Notification triggers include:

- Application Submitted
- Bookmark Created
- Interview Scheduled
- Candidate Selected
- Candidate Rejected
- Enterprise Approved

---

# Search Capabilities

Enterprise users can search students using:

- Discipline
- Skill Tags
- Graduation Year
- Project Type
- Institution
- Domain

---

# Feature Flags

Supports:

- Global Toggle
- Percentage Rollout
- Role-Based Rollout
- Feature Expiration
- Rollback Support

---

# Audit Logging

Every governed operation generates audit events.

Examples:

- Login
- Profile Update
- Enterprise Approval
- Application Status Change
- Role Change
- Feature Toggle
- Job Posting Approval

---

# Data Synchronization

Student data follows a Pull-on-Login synchronization strategy.

```
Student Login
      │
      ▼
OAuth Authentication
      │
      ▼
Call ZenCube REST API
      │
      ▼
Update Local Cache
      │
      ▼
Dashboard
```

---

# Performance Goals

| Metric | Target |
|---------|---------|
| Dashboard Load | <2 seconds |
| Search Response | <3 seconds |
| Supported Students | 1000+ |
| Availability | Best Effort |

---

# Documentation

Project documentation includes:

- Product Specification
- RFC Documentation
- ER Diagrams
- C4 Architecture
- API Contracts
- Database Design
- Service Layer Diagrams
- Sequence Diagrams
- Deployment Guide

---

# API Documentation

REST APIs include:

```
Authentication
Students
Enterprises
Job Openings
Applications
Hiring Pipeline
Notifications
Calendar
Chat
Analytics
Audit Logs
Feature Flags
```

---

# Future Roadmap

Planned post-V1 enhancements include:

- AI Candidate Matching
- Resume Parsing
- Enterprise Multi-User Support
- Mobile Applications
- Google Calendar Integration
- Billing & Subscription
- Advanced Analytics
- Career Readiness Scoring
- Interview Assessment Platform

---

# Contributors

**Product Owner**

Swathi (Chief Product Officer)

---

**Engineering Team**

ZenCube Talent Registry Development Team

---

**Technology**

- ReactJS
- Spring Boot
- PostgreSQL

---

# License

This project is proprietary software developed for **ZenV Quantum Private Limited**.

Unauthorized copying, distribution, or modification is prohibited.

---

# Contact

**Organization**

ZenV Quantum Private Limited

**Product**

ZenCube Talent Registry

**Status**

Version 1.0.0 (Draft)

---

> **"Building Verified Talent. Enabling Trusted Hiring."**
