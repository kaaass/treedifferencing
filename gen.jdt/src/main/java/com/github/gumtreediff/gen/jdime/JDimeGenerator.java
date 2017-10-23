/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2015-2017 Georg Dotzler <georg.dotzler@fau.de>
 * Copyright 2015-2017 Marius Kamp <marius.kamp@fau.de>
 */
package com.github.gumtreediff.gen.jdime;

import com.github.gumtreediff.gen.Register;
import com.github.gumtreediff.gen.Registry;
import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.tree.AstNodeArtifactWrapper;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;

import de.fosd.jdime.artifact.ast.ASTNodeArtifact;
import de.fosd.jdime.config.merge.Revision;

@Register(id = "java-jdime", accept = "\\.java$", priority = Registry.Priority.MAXIMUM)
public class JDimeGenerator extends TreeGenerator {
    @Override
    protected TreeContext generate(Reader r) throws IOException {
        StringBuilder fileData = new StringBuilder();
        try (BufferedReader br = new BufferedReader(r)) {
            char[] buf = new char[10];
            int numRead = 0;
            while ((numRead = br.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
        }

        Revision.SuccessiveRevSupplier revSupplier = new Revision.SuccessiveRevSupplier();

        final byte[] bytes = fileData.toString().getBytes();
        ASTNodeArtifact artifact =
                new ASTNodeArtifact(revSupplier.get(), new ByteArrayInputStream(bytes));
        AstNodeArtifactWrapper wrapper = new AstNodeArtifactWrapper(artifact);
        TreeContext context = new TreeContext();
        context.setRoot(wrapper);

        TreeUtils.computeSize(wrapper);
        AstNodeArtifactWrapper.computePositions(wrapper, bytes);
        return context;
    }

}

