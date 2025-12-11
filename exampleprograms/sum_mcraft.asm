# sum_mcraft.asm
# Sum 1+2+3+4+5 using M-CRAFT basic instructions.

        .text
        .globl main

main:
        # R0 -> $zero
        # R1 -> $t1 (loop counter / current addend)
        # R2 -> $t2 (running sum)
        # R3 -> $t3 (limit = 6)

        ADDI    $t1, $zero, 1
        ADDI    $t2, $zero, 0
        ADDI    $t3, $zero, 6

loop:
        ADD     $t2, $t2, $t1
        ADDI    $t1, $t1, 1
        BNE     $t1, $t3, loop

        # At this point, $t2 holds 1+2+3+4+5 = 15

        CHAT    $t2
        J       done

done:
        ADD     $zero, $zero, $zero
