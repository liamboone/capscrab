import re
import pdb

fid = open( "game1_500_3.txt" )
players = 3
rounds = 500

curRound = 0

bidsforletter = {}
for h in range(7):
    bidsforletter[h] = []
    for i in range(26):
        bidsforletter[h].append(set())

player={}

letter = lambda char : ord( char ) - 65

bidre = re.compile( "^.* letter (.) .* #([0-9]+) .* was ([0-9]+) .* of ([0-9]+)$" )
wordre = re.compile( "^.* #([0-9]) .* word ([A-Z]*) for ([0-9]+)$" )
seen = -1

for line in fid:
    if seen == -1:
        seen = 0
        for p in range(players):
            #current hand, ammount paid, bids, net, letters in hand
            player[p] = [[0]*26, 0, {}, 0, 0]
            for h in range(7):
                player[p][2][h] = []
                for i in range(26):
                    player[p][2][h].append([])
    
    bid = re.match( bidre, line )
    word = re.match( wordre, line )
    
    if bid:
        #compile data for current round
        letter = bid.group(1)
        pid = int( bid.group(2) )
        paid = int( bid.group(3) )
        offer = int( bid.group(4) )
        letord = ord(letter)-ord('A')
        player[pid][0][letord] += 1
        player[pid][1] += paid
        player[pid][2][player[pid][4]][letord].append(offer)
        player[pid][4] += 1
        
    if word:
        seen += 1
        pid = int( word.group(1) )
        play = word.group(2)
        score = int( word.group(3) )
        player[pid][3] = score - player[pid][1]

    if seen == 3:
        seen = -1
        pid = -1
        net = 0
        #find highest netting player
        for i in range(players):
            if player[i][3] > net:
                pid = i
                net = player[i][3]
        #add their bids to the bid list
        if pid in player:
            for h in range(7):
                for i in range(26):
                    bidsforletter[h][i] |= set( player[pid][2][h][i] )

for h in bidsforletter.keys():
    print ";".join([ ",".join({str(b) if b < 10 else '9' for b in bidsforletter[h][i]}) if bidsforletter[h][i] else "0" for i in range(26)])
