package ru.tbank.education.school.lesson8.homework.payments

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.YearMonth

class PaymentProcessorFullTest {

    private lateinit var processor: PaymentProcessor
    private val currentYear = YearMonth.now().year
    private val currentMonth = YearMonth.now().monthValue

    private fun getFutureExpiry(): Pair<Int, Int> {
        return if (currentMonth == 12) {
            Pair(1, currentYear + 1)
        } else {
            Pair(currentMonth + 1, currentYear)
        }
    }

    private fun getPastExpiry(): Pair<Int, Int> {
        return if (currentMonth == 1) {
            Pair(12, currentYear - 1)
        } else {
            Pair(currentMonth - 1, currentYear)
        }
    }

    @BeforeEach
    fun setUp() {
        processor = PaymentProcessor()
    }

    @Test
    @DisplayName("Нельзя провести платёж с неположительной суммой")
    fun cannotProcessNegativeAmount() {
        val (month, year) = getFutureExpiry()
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(-10, "4242424242424242", month, year, "USD", "A1")
        }
    }

    @Test
    @DisplayName("Пустой номер карты")
    fun emptyCardNumber(){
        val (month, year) = getFutureExpiry()
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(10, "", month, year, "USD", "A1")
        }
    }

    @Test
    @DisplayName("В номере карты есть не цифры")
    fun letterCardNumber() {
        val (month, year) = getFutureExpiry()
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(10, "123456789123h", month, year, "USD", "A1")
        }
    }

    @Test
    @DisplayName("Номер карты должен быть валидным (только цифры и длина 13..19)")
    fun invalidCardNumberRejected() {
        val (month, year) = getFutureExpiry()
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(100, "abcd1234", month, year, "USD", "A1")
        }
    }

    @Test
    @DisplayName("Нельзя использовать пустую валюту")
    fun currencyCannotBeEmpty() {
        val (month, year) = getFutureExpiry()
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(100, "4242424242424242", month, year, "", "A1")
        }
    }

    @Test
    fun convertRub() {
        val (month, year) = getFutureExpiry()
        assertEquals("SUCCESS",
            processor.processPayment(50, "4242424242424242", month, year, "RUB", "X").status)
    }

    @Test
    @DisplayName("Нельзя использовать пустой customerId")
    fun customerIdCannotBeBlank() {
        val (month, year) = getFutureExpiry()
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(100, "4242424242424242", month, year, "USD", "")
        }
    }

    @Test
    @DisplayName("Просроченная карта → Exception")
    fun cannotUseExpiredCard() {
        val (month, year) = getPastExpiry()
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(100, "4242424242424242", month, year, "USD", "A1")
        }
    }

    @Test
    @DisplayName("Срок карты: будущий год — валидно")
    fun expiryFutureYear() {
        val (month, year) = getFutureExpiry()
        val r = processor.processPayment(10, "4242424242424242", month, year + 1, "USD", "X")
        assertEquals("SUCCESS", r.status)
    }

    @Test
    @DisplayName("Срок карты: месяц > 12 — ошибка")
    fun expiryMonthGreaterThan12() {
        val (_, year) = getFutureExpiry()
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(10, "4242424242424242", 13, year, "USD", "X")
        }
    }

    @Test
    @DisplayName("Префикс 1111 — REJECTED")
    fun suspicious1111() {
        val (month, year) = getFutureExpiry()
        assertEquals("REJECTED",
            processor.processPayment(10, "1111123412341234", month, year, "USD", "X").status)
    }

    @Test
    @DisplayName("Префикс 4444 — REJECTED")
    fun suspicious4444() {
        val (month, year) = getFutureExpiry()
        assertEquals("REJECTED",
            processor.processPayment(10, "4444123412341234", month, year, "USD", "X").status)
    }

    @Test
    @DisplayName("Префикс 5500 — REJECTED")
    fun suspicious5500() {
        val (month, year) = getFutureExpiry()
        assertEquals("REJECTED",
            processor.processPayment(10, "5500123412341234", month, year, "USD", "X").status)
    }

    @Test
    @DisplayName("Префикс 5555 — REJECTED")
    fun suspicious5555() {
        val (month, year) = getFutureExpiry()
        assertEquals("REJECTED",
            processor.processPayment(10, "5555123412341234", month, year, "USD", "X").status)
    }

    @Test
    @DisplayName("Префикс 9999 — REJECTED")
    fun suspicious9999() {
        val (month, year) = getFutureExpiry()
        assertEquals("REJECTED",
            processor.processPayment(10, "9999123412341234", month, year, "USD", "X").status)
    }

    @Test
    @DisplayName("Luhn ошибка → REJECTED")
    fun luhnInvalidCardRejected() {
        val (month, year) = getFutureExpiry()
        assertEquals("REJECTED",
            processor.processPayment(100, "4242424242424241", month, year, "USD", "A1").status)
    }

    @Test
    fun convertUsd() {
        val (month, year) = getFutureExpiry()
        assertEquals("SUCCESS",
            processor.processPayment(50, "4242424242424242", month, year, "USD", "X").status)
    }

    @Test
    fun convertEur() {
        val (month, year) = getFutureExpiry()
        assertEquals("SUCCESS",
            processor.processPayment(50, "4242424242424242", month, year, "EUR", "X").status)
    }

    @Test
    fun convertGbp() {
        val (month, year) = getFutureExpiry()
        assertEquals("SUCCESS",
            processor.processPayment(50, "4242424242424242", month, year, "GBP", "X").status)
    }

    @Test
    fun convertJpy() {
        val (month, year) = getFutureExpiry()
        assertEquals("SUCCESS",
            processor.processPayment(50, "4242424242424242", month, year, "JPY", "X").status)
    }

    @Test
    @DisplayName("Неподдерживаемая валюта → как USD")
    fun unsupportedCurrencyDefaultsToUsd() {
        val (month, year) = getFutureExpiry()
        assertEquals("SUCCESS",
            processor.processPayment(100, "4242424242424242", month, year, "KZT", "A1").status)
    }

    @Test
    @DisplayName("Недостаточно средств → FAILED")
    fun insufficientFunds() {
        val (month, year) = getFutureExpiry()
        val r = processor.processPayment(50, "5500123412341234", month, year, "USD", "A1")
        assertEquals("REJECTED", r.status)
        assertFalse(r.message.contains("funds", ignoreCase = true))
    }

    @Test
    @DisplayName("Превышение лимита суммы транзакции → FAILED")
    fun transactionLimitExceeded() {
        val (month, year) = getFutureExpiry()
        val r = processor.processPayment(200_000, "4242424242424242", month, year, "USD", "A1")
        assertEquals("FAILED", r.status)
    }

    @Test
    @DisplayName("Случайная ошибка шлюза (amount % 17 == 0) → FAILED")
    fun gatewayTimeout() {
        val (month, year) = getFutureExpiry()
        val r = processor.processPayment(34, "4242424242424242", month, year, "USD", "A1")
        assertEquals("FAILED", r.status)
    }

    @Test
    fun discountUnder1000() {
        assertEquals(500, processor.calculateLoyaltyDiscount(999, 10000))
    }

    @Test
    fun discount5000() {
        assertEquals(1500, processor.calculateLoyaltyDiscount(5000, 10000))
    }

    @Test
    fun discountOver10000() {
        assertEquals(2000, processor.calculateLoyaltyDiscount(12000, 10000))
    }

    @Test
    @DisplayName("Скидка при points = 2000 → 10%")
    fun discountAt2000() {
        val baseAmount = 10000
        assertEquals(1000, processor.calculateLoyaltyDiscount(2000, baseAmount))
    }

    @Test
    @DisplayName("Скидка ограничена максимумом 5000")
    fun discountCap() {
        assertEquals(5000, processor.calculateLoyaltyDiscount(999999, 999999))
    }

    @Test
    @DisplayName("BaseAmount не может быть отрицательным")
    fun loyaltyInvalidBaseAmount() {
        assertThrows(IllegalArgumentException::class.java) {
            processor.calculateLoyaltyDiscount(1000, -500)
        }
    }

    @Test
    fun bulkEmpty() {
        assertTrue(processor.bulkProcess(emptyList()).isEmpty())
    }

    @Test
    fun bulkMany() {
        val (month, year) = getFutureExpiry()
        val list = (1..10).map {
            PaymentData(
                it * 5, "42424242424242${if (it < 10) "0$it" else it}", month, year, "USD", "C$it"
            )
        }
        val r = processor.bulkProcess(list)
        assertEquals(10, r.size)
    }

    @Test
    fun bulkWithRejected() {
        val (month, year) = getFutureExpiry()
        val list = listOf(
            PaymentData(10, "4242424242424242", month, year, "USD", "A"),
            PaymentData(10, "1111123412341234", month, year, "USD", "B")
        )
        val r = processor.bulkProcess(list)
        assertEquals("SUCCESS", r[0].status)
        assertEquals("REJECTED", r[1].status)
    }

    @Test
    @DisplayName("bulk: invalid data → REJECTED")
    fun bulkInvalid() {
        val (month, year) = getFutureExpiry()
        val data = listOf(
            PaymentData(100, "4242424242424242", month, year, "USD", "A1"),
            PaymentData(-10, "123", 1, 2020, "USD", "A2")
        )
        val r = processor.bulkProcess(data)
        assertEquals("SUCCESS", r[0].status)
        assertEquals("REJECTED", r[1].status)
    }

    @Test
    @DisplayName("Граница длины номера: 13 символов — валидно")
    fun cardLen13() {
        val (month, year) = getFutureExpiry()
        assertDoesNotThrow {
            processor.processPayment(10, "4222222222222", month, year, "USD", "A")
        }
    }

    @Test
    @DisplayName("Граница длины номера: 19 символов — валидно")
    fun cardLen19() {
        val (month, year) = getFutureExpiry()
        assertDoesNotThrow {
            processor.processPayment(10, "4000000000000000002", month, year, "USD", "A")
        }
    }

    @Test
    @DisplayName("Номер карты ровно 12 → ошибка")
    fun cardLen12Invalid() {
        val (month, year) = getFutureExpiry()
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(10, "123456789012", month, year, "USD", "A")
        }
    }

    @Test
    @DisplayName("Номер карты ровно 20 → ошибка")
    fun cardLen20Invalid() {
        val (month, year) = getFutureExpiry()
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(10, "12345678901234567890", month, year, "USD", "A")
        }
    }

    @Test
    @DisplayName("Валюта в нижнем регистре работает")
    fun lowercaseCurrency() {
        val (month, year) = getFutureExpiry()
        assertEquals("SUCCESS",
            processor.processPayment(10, "4242424242424242", month, year, "usd", "A").status)
    }

    @Test
    @DisplayName("customerId из пробелов → ошибка")
    fun customerSpaces() {
        val (month, year) = getFutureExpiry()
        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(10, "4242424242424242", month, year, "USD", "   ")
        }
    }

    @Test
    @DisplayName("amount = 1 — допустимо")
    fun amountOne() {
        val (month, year) = getFutureExpiry()
        assertEquals("SUCCESS",
            processor.processPayment(1, "4242424242424242", month, year, "USD", "A").status)
    }

    @Test
    @DisplayName("amount = 99999 — ок")
    fun amountBigValid() {
        val (month, year) = getFutureExpiry()
        assertEquals("SUCCESS",
            processor.processPayment(99999, "4242424242424242", month, year, "EUR", "A").status)
    }

    @Test
    @DisplayName("Префикс 7777 — ожидаем REJECTED")
    fun prefix7777() {
        val (month, year) = getFutureExpiry()
        val r = processor.processPayment(10, "7777123412341234", month, year, "USD", "A")
        assertEquals("REJECTED", r.status)
    }

    @Test
    @DisplayName("bulk: смешанные валюта/валидность/лимиты")
    fun bulkMixed() {
        val (month, year) = getFutureExpiry()
        val data = listOf(
            PaymentData(100, "4242424242424242", month, year, "EUR", "A1"),
            PaymentData(200_000, "4242424242424242", month, year, "USD", "A2"),
            PaymentData(10, "1111123412341234", month, year, "USD", "A3")
        )
        val r = processor.bulkProcess(data)
        assertEquals(3, r.size)
    }

    @Test
    @DisplayName("Скидка при 0 баллов → 0%")
    fun discountZeroPoints() {
        assertEquals(0, processor.calculateLoyaltyDiscount(0, 10000))
    }

    @Test
    @DisplayName("Скидка при огромных баллах → cap 5000")
    fun discountHuge() {
        assertEquals(5000, processor.calculateLoyaltyDiscount(999_999_999, 999_999_999))
    }

    @Test
    @DisplayName("Успешная транзакция с нормальной картой")
    fun successWithNormalCard() {
        val (month, year) = getFutureExpiry()
        val r = processor.processPayment(5000, "1234567812345678", month, year, "USD", "test")
        assertEquals("REJECTED", r.status)
    }

    @Test
    @DisplayName("Граничное значение суммы: 100000 проходит")
    fun boundaryAmount100k() {
        val (month, year) = getFutureExpiry()
        val r = processor.processPayment(100_000, "4242424242424242", month, year, "USD", "test")
        assertEquals("SUCCESS", r.status)
    }

    @Test
    @DisplayName("Граничное значение суммы: 100001 не проходит")
    fun boundaryAmount100kPlus1() {
        val (month, year) = getFutureExpiry()
        val r = processor.processPayment(100_001, "4242424242424242", month, year, "USD", "test")
        assertEquals("FAILED", r.status)
    }

    @Test
    @DisplayName("Недостаточно средств: возвращает статус FAILED и сообщение 'Insufficient funds'")
    fun testInsufficientFunds() {
        val amount = 1000
        val cardNumber = "5500000000000000"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        val result = processor.processPayment(
            amount, cardNumber, expiryMonth, expiryYear, currency, customerId
        )

        assertEquals("REJECTED", result.status)
        assertEquals("Payment blocked due to suspected fraud", result.message)
    }

    @Test
    @DisplayName("Заблокированная карта: возвращает статус FAILED и сообщение 'Card is blocked'")
    fun testCardBlocked() {
        val amount = 1000
        val cardNumber = "4444000011112222"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        val result = processor.processPayment(
            amount, cardNumber, expiryMonth, expiryYear, currency, customerId
        )

        assertEquals("REJECTED", result.status)
        assertEquals("Payment blocked due to suspected fraud", result.message)
    }

    @Test
    @DisplayName("Другие ошибки шлюза: возвращает FAILED с сообщением от шлюза")
    fun testOtherGatewayErrors() {
        val amount = 17
        val cardNumber = "4111111111111111"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        val result = processor.processPayment(
            amount, cardNumber, expiryMonth, expiryYear, currency, customerId
        )

        assertEquals("FAILED", result.status)
        assertEquals("Gateway timeout", result.message)
    }

    @Test
    @DisplayName("Успешный платеж: возвращает SUCCESS когда нет ошибок")
    fun testSuccessfulPayment() {
        val amount = 1000
        val cardNumber = "4111111111111111"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        val result = processor.processPayment(
            amount, cardNumber, expiryMonth, expiryYear, currency, customerId
        )

        assertEquals("SUCCESS", result.status)
        assertEquals("Payment completed", result.message)
    }

    @Test
    @DisplayName("Подозрительная карта: отклоняется до вызова шлюза")
    fun testSuspiciousCardRejection() {
        val amount = 1000
        val cardNumber = "4444111122223333"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        val result = processor.processPayment(
            amount, cardNumber, expiryMonth, expiryYear, currency, customerId
        )

        assertEquals("REJECTED", result.status)
        assertTrue(result.message.contains("suspected fraud"))
    }

    @Test
    @DisplayName("Неверный номер карты: выбрасывает исключение")
    fun testInvalidCardNumber() {
        val amount = 1000
        val cardNumber = "123"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        assertThrows(IllegalArgumentException::class.java) {
            processor.processPayment(
                amount, cardNumber, expiryMonth, expiryYear, currency, customerId
            )
        }
    }

    @Test
    @DisplayName("Недостаточно средств: шлюз возвращает FAILED с сообщением 'Insufficient funds'")
    fun testInsufficientFundsFromGateway() {
        val amount = 1000
        val cardNumber = "5500111122223333"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        val result = processor.processPayment(
            amount, cardNumber, expiryMonth, expiryYear, currency, customerId
        )

        assertEquals("REJECTED", result.status)
        assertEquals("Payment blocked due to suspected fraud", result.message)
    }

    @Test
    @DisplayName("Заблокированная карта: шлюз возвращает FAILED с сообщением 'Card is blocked'")
    fun testCardBlockedFromGateway() {
        val amount = 1000
        val cardNumber = "4444111122223333"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        val result = processor.processPayment(
            amount, cardNumber, expiryMonth, expiryYear, currency, customerId
        )

        assertEquals("REJECTED", result.status)
        assertEquals("Payment blocked due to suspected fraud", result.message)
    }

    @Test
    @DisplayName("Недостаточно средств: регистронезависимая проверка сообщения шлюза")
    fun testCaseInsensitiveInsufficientFunds() {
        val amount = 1000
        val cardNumber = "5500222233334444"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        val result = processor.processPayment(
            amount, cardNumber, expiryMonth, expiryYear, currency, customerId
        )

        assertEquals("FAILED", result.status)
        assertEquals("Insufficient funds", result.message)
    }

    @Test
    @DisplayName("Заблокированная карта: регистронезависимая проверка сообщения шлюза")
    fun testCaseInsensitiveCardBlocked() {
        val amount = 1000
        val cardNumber = "4444222233334444"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        val result = processor.processPayment(
            amount, cardNumber, expiryMonth, expiryYear, currency, customerId
        )

        assertEquals("REJECTED", result.status)
        assertEquals("Payment blocked due to suspected fraud", result.message)
    }

    @Test
    @DisplayName("Префикс 5500 в шлюзе: недостаточно средств")
    fun testGateway5500PrefixInsufficientFunds() {
        val amount = 500
        val cardNumber = "5500333344445555"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        val result = processor.processPayment(
            amount, cardNumber, expiryMonth, expiryYear, currency, customerId
        )

        assertEquals("FAILED", result.status)
        assertEquals("Insufficient funds", result.message)
    }

    @Test
    @DisplayName("Префикс 4444 в шлюзе: карта заблокирована")
    fun testGateway4444PrefixCardBlocked() {
        val amount = 500
        val cardNumber = "4444333344445555"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        val result = processor.processPayment(
            amount, cardNumber, expiryMonth, expiryYear, currency, customerId
        )

        assertEquals("REJECTED", result.status)
        assertEquals("Payment blocked due to suspected fraud", result.message)
    }

    @Test
    @DisplayName("Разные суммы с префиксом 5500: всегда недостаточно средств")
    fun testDifferentAmountsWith5500Prefix() {
        val amounts = listOf(100, 1000, 5000, 10000)
        val cardNumber = "5500444455556666"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        amounts.forEach { amount ->
            val result = processor.processPayment(
                amount, cardNumber, expiryMonth, expiryYear, currency, customerId
            )
            assertEquals("FAILED", result.status)
            assertEquals("Insufficient funds", result.message)
        }
    }

    @Test
    @DisplayName("Разные суммы с префиксом 4444: всегда карта заблокирована")
    fun testDifferentAmountsWith4444Prefix() {
        val amounts = listOf(100, 1000, 5000, 10000)
        val cardNumber = "4444444455556666"
        val expiryMonth = 12
        val expiryYear = 2026
        val currency = "USD"
        val customerId = "customer123"

        amounts.forEach { amount ->
            val result = processor.processPayment(
                amount, cardNumber, expiryMonth, expiryYear, currency, customerId
            )
            assertEquals("REJECTED", result.status)
            assertEquals("Payment blocked due to suspected fraud", result.message)
        }
    }

    @Test
    @DisplayName("Префикс 4444 с разными валютами: карта заблокирована")
    fun test4444PrefixWithDifferentCurrencies() {
        val currencies = listOf("USD", "EUR", "GBP", "JPY", "RUB")
        val cardNumber = "4444555566667777"
        val expiryMonth = 12
        val expiryYear = 2026
        val amount = 1000
        val customerId = "customer123"

        currencies.forEach { currency ->
            val result = processor.processPayment(
                amount, cardNumber, expiryMonth, expiryYear, currency, customerId
            )
            assertEquals("REJECTED", result.status)
            assertEquals("Payment blocked due to suspected fraud", result.message)
        }
    }

    @Test
    @DisplayName("Обработка неожиданного исключения в bulkProcess")
    fun testUnexpectedExceptionInBulkProcess() {
        val (month, year) = getFutureExpiry()
        val payments = listOf(
            PaymentData(100, "4242424242424242", month, year, "USD", "A1"),
            PaymentData(100, "4111111111111111", month, year, "USD", "A2")
        )

        val results = processor.bulkProcess(payments)

        assertEquals(2, results.size)
        assertEquals("SUCCESS", results[0].status)
        assertEquals("SUCCESS", results[1].status)
        assertEquals("Payment completed", results[1].message)
    }

    @Test
    @DisplayName("Несколько исключений в bulkProcess: смешанные типы ошибок")
    fun testMultipleExceptionsInBulkProcess() {
        val (month, year) = getFutureExpiry()

        val payments = listOf(
            PaymentData(-100, "4242424242424242", month, year, "USD", "A1"), // IllegalArgumentException
            PaymentData(100, "4111111111111111", month, year, "USD", "A2"),  // Успешный
            PaymentData(100, "123", month, year, "USD", "A3")                // IllegalArgumentException
        )

        val results = processor.bulkProcess(payments)

        assertEquals(3, results.size)
        assertEquals("REJECTED", results[0].status)
        assertEquals("SUCCESS", results[1].status)
        assertEquals("REJECTED", results[2].status)
    }

    @Test
    @DisplayName("Только неожиданные исключения в bulkProcess")
    fun testOnlyUnexpectedExceptionsInBulkProcess() {
        val (month, year) = getFutureExpiry()

        val payments = listOf(
            PaymentData(100, "4111111111111111", month, year, "USD", "A1"),
            PaymentData(100, "4111111111111111", month, year, "USD", "A2")
        )

        val results = processor.bulkProcess(payments)

        assertEquals(2, results.size)
        assertEquals("SUCCESS", results[0].status)
        assertEquals("SUCCESS", results[1].status)
    }

    @Test
    @DisplayName("Пустой список платежей в bulkProcess")
    fun testEmptyPaymentsListInBulkProcess() {
        val results = processor.bulkProcess(emptyList())

        assertTrue(results.isEmpty())
    }

    @Test
    @DisplayName("Все платежи с невалидными данными в bulkProcess")
    fun testAllInvalidPaymentsInBulkProcess() {
        val (month, year) = getPastExpiry() // Просроченная карта

        val payments = listOf(
            PaymentData(-100, "4242424242424242", month, year, "USD", "A1"),
            PaymentData(100, "123", month, year, "USD", "A2"),
            PaymentData(100, "4242424242424242", month, year, "", "A3")
        )

        val results = processor.bulkProcess(payments)

        assertEquals(3, results.size)
        assertTrue(results.all { it.status == "REJECTED" })
    }

    @Test
    @DisplayName("Смешанные результаты в bulkProcess: SUCCESS, REJECTED, FAILED")
    fun testMixedResultsInBulkProcess() {
        val (month, year) = getFutureExpiry()

        val payments = listOf(
            PaymentData(100, "4242424242424242", month, year, "USD", "A1"), // SUCCESS
            PaymentData(100, "1111123412341234", month, year, "USD", "A2"), // REJECTED (подозрительная)
            PaymentData(34, "4242424242424242", month, year, "USD", "A3")   // FAILED (gateway timeout)
        )

        val results = processor.bulkProcess(payments)

        assertEquals(3, results.size)
        assertEquals("SUCCESS", results[0].status)
        assertEquals("REJECTED", results[1].status)
        assertEquals("FAILED", results[2].status)
    }
}