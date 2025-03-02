# TC-POS Frontend

A Java Swing frontend for the TC-POS system, connecting to the backend for sales and receipt printing.

## Features
- Simple UI: Scan products, manage cart, checkout.
- Displays prices in ZAR (R).
- Prints receipts to a thermal printer or console.

## Setup
1. **Clone**: `git clone https://github.com/kholsas/tc-pos-frontend.git`
2. **Build**: `mvn clean install`
3. **Run Backend**: Ensure `tc-pos-backend` is running at `http://localhost:8080`.
4. **Run**: `java -jar target/tc-pos-frontend-1.0-SNAPSHOT.jar`

## Usage
- Scan: Enter barcode (e.g., "10000") and press Enter.
- Remove: Select item, click "Remove Selected".
- Checkout: Click "Checkout" to process and print.

## Requirements
- Java 17
- Maven 3.8.7+
- Backend running

## Structure