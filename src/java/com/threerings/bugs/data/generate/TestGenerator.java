//
// $Id$

package com.threerings.bugs.data.generate;

import java.util.ArrayList;

import com.threerings.bugs.data.BugsBoard;
import com.threerings.bugs.data.Terrain;
import com.threerings.bugs.data.pieces.Ant;
import com.threerings.bugs.data.pieces.AntHill;
import com.threerings.bugs.data.pieces.Bee;
import com.threerings.bugs.data.pieces.Frog;
import com.threerings.bugs.data.pieces.Leaf;
import com.threerings.bugs.data.pieces.Piece;
import com.threerings.bugs.data.pieces.SodaDrop;
import com.threerings.bugs.data.pieces.Tree;

/**
 * A board generator that does a one shot generation of a simple test board.
 */
public class TestGenerator
{
    public void generate (BugsBoard board, ArrayList<Piece> pieces)
    {
        for (int xx = 0; xx < board.getWidth(); xx++) {
            board.setTile(xx, 4, Terrain.WATER);
            board.setTile(xx, 5, Terrain.WATER);
        }

        for (int ii = 0; ii < 2; ii++) {
            Ant ant = new Ant();
            ant.position(ii+4, 8+(ii%2));
            pieces.add(ant);
        }

        Bee bee = new Bee();
        bee.position(7, 8);
        pieces.add(bee);

        for (int ii = 0; ii < 2; ii++) {
            Leaf leaf = new Leaf();
            leaf.position(ii+3, 7);
            pieces.add(leaf);
        }

        Frog frog = new Frog();
        frog.position(0, 6);
        frog.rotate(Piece.CW);
        pieces.add(frog);

        Tree tree = new Tree();
        tree.position(6, 1);
        pieces.add(tree);

        SodaDrop food = new SodaDrop();
        food.position(4, 2);
        pieces.add(food);
        food = new SodaDrop();
        food.position(3, 3);
        pieces.add(food);

        AntHill hill = new AntHill();
        hill.position(0, 0);
        pieces.add(hill);
    }
}
