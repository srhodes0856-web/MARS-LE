M-CRAFT Language — MARS LE Custom Assembly Extension

CS210 Final Project

Author: Sam Rhodes

Overview

M-CRAFT is a custom 32-bit assembly language inspired by MIPS but themed around
Minecraft mechanics. The language supports normal arithmetic and branching
operations, as well as Minecraft-themed instructions such as WORLD, INVENTORY,
and syscall-like operations (MINE, PLACE, CRAFT, HOTBAR, CHAT, etc.).

Implemented Instructions
Basic MIPS-Style Instructions (14)

ADD, SUB, AND, OR, XOR, SLT

ADDI, ANDI, ORI

LW, SW

BEQ, BNE

J

Themed M-CRAFT Instructions (11)

MINE — read block from WORLD

PLACE — write block to WORLD

CRAFT — pack lower 16 bits of two registers

BREW — nonlinear mix function

SMELT — clamp/normalize

HOTBAR — updates HB register ($k0)

INVLD — load from INVENTORY

INVST — store to INVENTORY

CHAT — syscall-like print setup ($a0, $v0 = 1)

SPRINT — counted loop instruction

IFMOB — simulated mob detection

WORLD and INVENTORY memory are mapped to safe MARS regions:
WORLD_BASE = 0x10010000
INVENTORY_BASE = 0x10020000

How to Build and Run in MARS LE:

Build the Custom Language

From the root MARS-LE directory, run:

java -jar BuildCustomLang.jar MCraftLanguage.jar

Place the resulting JAR into:

mars/mips/instructions/customlangs/

Instructions for activating the language inside MARS LE:

Tools -> Language Switcher -> M-CRAFT Language -> Apply -> Open file -> Assemble
