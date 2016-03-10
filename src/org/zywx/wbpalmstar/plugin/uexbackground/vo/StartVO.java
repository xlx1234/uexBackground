/*
 * Copyright (c) 2016.  The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.zywx.wbpalmstar.plugin.uexbackground.vo;

import java.io.Serializable;

/**
 * Created by ylt on 16/3/7.
 */
public class StartVO implements Serializable {

    private static final long serialVersionUID = -6712534922362952494L;

    private String jsPath;

    private String[] jsResourcePaths;

    public String getJsPath() {
        return jsPath;
    }

    public void setJsPath(String jsPath) {
        this.jsPath = jsPath;
    }

    public String[] getJsResourcePaths() {
        return jsResourcePaths;
    }

    public void setJsResourcePaths(String[] jsResourcePaths) {
        this.jsResourcePaths = jsResourcePaths;
    }
}
