# fizzbuzz_mcraft.asm
        .text
        .globl main

main:
        # Mapping for readability:
        # R1 -> $t1  (current number i)
        # R2 -> $t2  (end = 16)
        # R3 -> $t3  (counter for 3)
        # R4 -> $t4  (counter for 5)
        # R5 -> $t5  (flag: 0=number, 1=fizz, 2=buzz, 3=fizzbuzz)
        # R6 -> $t6  (temp)
        # R7 -> $t7  (print code: 1,2,3)

        ADDI    $t1, $zero, 1
        ADDI    $t2, $zero, 16
        ADDI    $t3, $zero, 3
        ADDI    $t4, $zero, 5

loop:
        ADDI    $t5, $zero, 0 # R5 = 0 (reset flag each iteration)

        # Decrement counters for 3 and 5
        ADDI    $t3, $t3, -1
        ADDI    $t4, $t4, -1

        # Check fizz (multiple of 3)
        BNE     $t3, $zero, check_buzz
        ADDI    $t5, $t5, 1
        ADDI    $t3, $zero, 3

check_buzz:
        BNE     $t4, $zero, decide_print
        ADDI    $t5, $t5, 2
        ADDI    $t4, $zero, 5

decide_print:
        BNE     $t5, $zero, check_fizzbuzz

        # If R5 == 0, print the number
        CHAT    $t1
        J       after_print

check_fizzbuzz:
        ADDI    $t6, $zero, 3
        BEQ     $t5, $t6, print_fizzbuzz

        ADDI    $t6, $zero, 1
        BEQ     $t5, $t6, print_fizz

        ADDI    $t6, $zero, 2
        BEQ     $t5, $t6, print_buzz

        J       after_print

print_fizz:
        ADDI    $t7, $zero, 1 # code 1 = fizz
        CHAT    $t7
        J       after_print

print_buzz:
        ADDI    $t7, $zero, 2 # code 2 = buzz
        CHAT    $t7
        J       after_print

print_fizzbuzz:
        ADDI    $t7, $zero, 3 # code 3 = fizzbuzz
        CHAT    $t7
        J       after_print

after_print:
        ADDI    $t1, $t1, 1 
        BNE     $t1, $t2, loop

        J       done

done:
        ADD     $zero, $zero, $zero
