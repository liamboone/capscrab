import re

fid = open( "game1_500_3.txt" )
players = 3
rounds = 500

curRound = 0
player={}
player[0] = [0]*26

letter = lambda char : ord( char ) - 65

bidre = re.compile( "^.* letter (.) .* #([0-9]+) .* was ([0-9]+) .* of ([0-9]+)$" )
wordre = re.compile( "^.* #([0-9]) .* word ([A-Z]*) for ([0-9]+)$" )

for line in fid:
    bid = re.match( bidre, line )
    word = re.match( wordre, line )
    
    if not bid and not word:
        print "Error!"
        break
    
    if bid:
        letter = bid.group(1)
        player = int( bid.group(2) )
        paid = int( bid.group(3) )
        offer = int( bid.group(4) )
        
    if word:
        player = int( word.group(1) )
        play = word.group(2)
        score = int( word.group(3) )
