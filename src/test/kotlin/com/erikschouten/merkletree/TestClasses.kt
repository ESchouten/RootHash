package com.erikschouten.merkletree

data class TestTradelane(val id: String, val transporter: Transporter)
data class TestTransporter(val name: String, val employeeCount: Int) : Transporter
interface Transporter