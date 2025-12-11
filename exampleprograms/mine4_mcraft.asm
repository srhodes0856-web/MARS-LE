# mine4_mcraft.asm
# Mine 4 consecutive blocks from WORLD at R5, store to INVENTORY via R6,
# then place them starting at WORLD at R4.

        .text
        .globl main

main:
        # R0 -> $zero
        # R1 -> $t1  (loop counter)
        # R2 -> $t2  (temp block ID)
        # R3 -> $t3  (loop limit = 4)
        # R4 -> $t4  (destination WORLD base)
        # R5 -> $t5  (source WORLD base / block pointer)
        # R6 -> $t6  (INVENTORY pointer)

        # Initialize counters
        ADDI    $t1, $zero, 0     # R1 = 0
        ADDI    $t3, $zero, 4     # R3 = 4 (we’ll mine 4 blocks)

        # Assume R5 ($t5) and R6 ($t6) are pre-set by caller
        # to starting WORLD and INVENTORY indices.
        # You can also init them here if you want:
        # ADDI  $t5, $zero, 0     # R5 = 0
        # ADDI  $t6, $zero, 0     # R6 = 0

mine_loop:
        MINE    $t2, 0($t5)       # R2 = WORLD[R5 + 0]
        INVST   $t2, 0($t6)       # INVENTORY[R6 + 0] = R2

        ADDI    $t5, $t5, 1       # R5++ (next world block)
        ADDI    $t6, $t6, 1       # R6++ (next inventory slot)
        ADDI    $t1, $t1, 1       # R1++ (count)

        BNE     $t1, $t3, mine_loop

        # Reset for place loop
        ADDI    $t1, $zero, 0     # R1 = 0
        ADDI    $t6, $t6, -4      # R6 -= 4 (go back to first stored inventory slot)

place_loop:
        INVLD   $t2, 0($t6)       # R2 = INVENTORY[R6]
        PLACE   $t2, 0($t4)       # WORLD[R4] = R2

        ADDI    $t4, $t4, 1       # R4++ (next dest block)
        ADDI    $t6, $t6, 1       # R6++ (next inventory)
        ADDI    $t1, $t1, 1       # R1++ (count)

        BNE     $t1, $t3, place_loop

        CHAT    $zero             # maybe “done” indicator; prints 0 in your spec
        J       done

done:
        ADD     $zero, $zero, $zero
