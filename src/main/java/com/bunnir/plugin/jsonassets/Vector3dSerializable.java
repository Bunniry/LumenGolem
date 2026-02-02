package com.bunnir.plugin.jsonassets;

import com.hypixel.hytale.math.vector.Vector3d;

import java.io.Serializable;

public class Vector3dSerializable implements Serializable {
    double x;
    double y;
    double z;
    public Vector3dSerializable(Vector3d vec)
    {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public  Vector3d Deserialize() {
        return new Vector3d(x, y, z);
    }
}
