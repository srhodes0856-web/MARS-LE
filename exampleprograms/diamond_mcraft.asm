# diamond_mcraft.asm
# Count how many WORLD cells contain DIAMOND, using MINE + SPRINT.

        .text
        .globl main

main:
        # R0 -> $zero
        # R1 -> $t1 (loop count = 10)
        # R2 -> $t2 (count of diamonds)
        # R3 -> $t3 (DIAMOND block ID)
        # R4 -> $t4 (temp block from WORLD)
        # R5 -> $t5 (block pointer in WORLD)

        ADDI    $t1, $zero, 10 # R1 = 10 cells to check
        ADDI    $t2, $zero, 0 # R2 = 0 (diamond count)

        # Assume R3 ($t3) is DIAMOND ID and R5 ($t5) is starting WORLD index
        # e.g., you could do:
        # ADDI  $t3, $zero, 42 # DIAMOND ID = 42
        # ADDI  $t5, $zero, 0

diamond_loop:
        MINE    $t4, 0($t5) # R4 = WORLD[R5]

        BNE     $t4, $t3, no_diamond
        ADDI    $t2, $t2, 1 # if block == DIAMOND, count++

no_diamond:
        ADDI    $t5, $t5, 1 # advance WORLD pointer

        # SPRINT R1, diamond_loop
        # Our SPRINT decrements R1 and branches while R1 != 0
        SPRINT  $t1, diamond_loop

done:
        CHAT    $t2
        ADD     $zero, $zero, $zero
