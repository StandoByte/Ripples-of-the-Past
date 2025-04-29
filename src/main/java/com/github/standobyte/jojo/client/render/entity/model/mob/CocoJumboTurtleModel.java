package com.github.standobyte.jojo.client.render.entity.model.mob;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.bb.ParseGenericModel;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.TurtleModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.passive.TurtleEntity;

public class CocoJumboTurtleModel<T extends TurtleEntity> extends TurtleModel<T> {
    public boolean hasKey;
    private final ModelRenderer mrPresidentKey;
    
    public CocoJumboTurtleModel(float inflate) {
        super(inflate);

        ParseGenericModel.ModelParsed.ElementMesh meshParsed = ParseGenericModel.GSON.fromJson(COCO_JUMBO_MESH, ParseGenericModel.ModelParsed.ElementMesh.class);
        ModelRenderer.ModelBox shellMesh = meshParsed.makeCube(new float[] { body.x, body.y + 2, body.z }, texWidth, texHeight);
        ClientReflection.getCubes(body).set(0, shellMesh);
        
        mrPresidentKey = new ModelRenderer(this);
        mrPresidentKey.setPos(0.0F, 11.0F, -10.0F);
        mrPresidentKey.texOffs(89, 1).addBox(-0.5F, 4.9F, 12.0F, 1.0F, 1.0F, 7.0F, -0.1F, false);
        mrPresidentKey.texOffs(105, 7).addBox(-0.5F, 4.9F, 13.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        mrPresidentKey.texOffs(98, 5).addBox(-1.25F, 4.9F, 16.0F, 1.0F, 1.0F, 2.0F, -0.15F, false);

        ModelRenderer mrPresidentKey_r1 = new ModelRenderer(this);
        mrPresidentKey_r1.setPos(0.0F, 4.95F, 9.85F);
        mrPresidentKey.addChild(mrPresidentKey_r1);
        ClientUtil.setRotationAngle(mrPresidentKey_r1, 0.0F, 0.7854F, 0.0F);
        mrPresidentKey_r1.texOffs(64, 5).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 1.0F, 3.0F, -0.15F, false);
        mrPresidentKey_r1.texOffs(76, 5).addBox(-1.5F, -0.8F, -1.5F, 3.0F, 1.0F, 3.0F, -0.25F, false);

        ModelRenderer mrPresidentKey_r2 = new ModelRenderer(this);
        mrPresidentKey_r2.setPos(0.0F, 5.4F, 9.85F);
        mrPresidentKey.addChild(mrPresidentKey_r2);
        ClientUtil.setRotationAngle(mrPresidentKey_r2, 0.0F, -0.7854F, 0.0F);
        mrPresidentKey_r2.texOffs(80, 0).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F, 0.1F, false);
        mrPresidentKey_r2.texOffs(64, 0).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F, 0.0F, false);
    }
    
    @Override
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        mrPresidentKey.xRot = body.xRot - ((float)Math.PI / 2F);
        mrPresidentKey.yRot = body.yRot;
        mrPresidentKey.zRot = body.zRot;
    }
    
    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(mrPresidentKey));
    }

    @Override
    public void renderToBuffer(MatrixStack pMatrixStack, IVertexBuilder pBuffer, 
            int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        super.renderToBuffer(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
    }
    
    public static final String COCO_JUMBO_MESH = 
            "{\"name\":\"bodyKeyhole\",\"color\":5,\"origin\":[0,0,0],\"rotation\":[0,0,0],\"export\":true,\"visibility\":true,\"locked\":false,\"render_order\":\"default\",\"allow_mirror_modeling\":true,\"vertices\":{\"hKMU\":[9.5,10,-14],\"xd4F\":[9.5,10,-20],\"r4ao\":[9.5,-10,-14],\"J2f2\":[9.5,-10,-20],\"FG7v\":[-9.5,10,-14],\"TJ68\":[-9.5,10,-20],\"yLwd\":[-9.5,-10,-14],\"2vAk\":[-9.5,-10,-20],\"QLYN\":[2.998143389165918,3.1529571736839674,-14],\"U073\":[-0.01573336239658185,6.130079330716596,-13.999999999999941],\"dL0o\":[-2.956100924896582,3.1897117682152363,-14],\"dgnq\":[-0.493543091302833,0.653644745557608,-13.999999999999943],\"0WGP\":[-0.493543091302833,-5.962182270070588,-14],\"dBZA\":[0.49883096104091673,-5.962182270070588,-13.999999999999943],\"hqo1\":[0.49883096104091673,-5.006562812257609,-13.999999999999943],\"189q\":[1.2706774461971695,-5.006562812257609,-13.999999999999943],\"9idl\":[1.2706774461971695,-3.0218147075692317,-13.999999999999941],\"UeaZ\":[1.2706774461971695,-5.006562812257609,-13.999999999999943],\"JXoa\":[1.2706774461971695,-3.0218147075692317,-13.999999999999941],\"e9Nq\":[0.49883096104091673,-2.98506011303796,-13.999999999999941],\"NKg3\":[0.5355855555721674,0.6903993400888755,-13.999999999999943],\"Fol7\":[-0.0340900248300681,6.079915213343185,-16.094368188804985],\"VVBW\":[0.4804742986074304,-3.0352242304113712,-16.094368188804985],\"F95B\":[1.2523207837636834,-3.0719788249426427,-16.094368188804985],\"1gaV\":[1.2523207837636834,-5.05672692963102,-16.094368188804985],\"xr2V\":[0.4804742986074304,-5.05672692963102,-16.094368188804985],\"BxC4\":[-2.9744575873300683,3.139547650841825,-16.094368188805042],\"qYIa\":[2.979786726732432,3.1027930563105564,-16.094368188805042],\"0nXS\":[1.2706774461971695,-5.006562812257611,-13.999999999999943],\"fqfV\":[1.2706774461971695,-3.021814707569232,-13.99999999999994],\"Itfe\":[-0.0340900248300681,6.079915213343185,-16.094368188804985],\"Sfyl\":[-0.5368997537363194,0.6034806281841969,-16.094368188804985],\"19bj\":[0.517228893138681,0.6402352227154644,-16.094368188804985],\"1pYf\":[0.4804742986074304,-3.035224230411371,-16.094368188804985],\"seOF\":[1.2523207837636834,-3.0719788249426423,-16.094368188804985],\"YR8Q\":[1.2523207837636834,-5.05672692963102,-16.094368188804985],\"M5gB\":[0.4804742986074304,-5.05672692963102,-16.094368188804985],\"hH9w\":[0.4804742986074304,-6.0123463874439995,-16.094368188804985],\"508A\":[-0.5368997537363194,-6.0123463874439995,-16.094368188805042],\"pECK\":[-2.9744575873300683,3.139547650841825,-16.094368188805042],\"U1HT\":[2.979786726732432,3.1027930563105564,-16.094368188805042]},\"faces\":{\"G14j2b5h\":{\"uv\":{\"xd4F\":[13,43],\"hKMU\":[7,43],\"J2f2\":[13,63],\"r4ao\":[7,63]},\"vertices\":[\"xd4F\",\"hKMU\",\"J2f2\",\"r4ao\"],\"texture\":0},\"rswr1An8\":{\"uv\":{\"xd4F\":[13,43],\"TJ68\":[32,43],\"hKMU\":[13,37],\"FG7v\":[32,37]},\"vertices\":[\"xd4F\",\"TJ68\",\"hKMU\",\"FG7v\"],\"texture\":0},\"kBARzAob\":{\"uv\":{\"r4ao\":[32,37],\"yLwd\":[51,37],\"J2f2\":[32,43],\"2vAk\":[51,43]},\"vertices\":[\"r4ao\",\"yLwd\",\"J2f2\",\"2vAk\"],\"texture\":0},\"ftsL30EL\":{\"uv\":{\"TJ68\":[32,43],\"xd4F\":[13,43],\"2vAk\":[32,63],\"J2f2\":[13,63]},\"vertices\":[\"TJ68\",\"xd4F\",\"2vAk\",\"J2f2\"],\"texture\":0},\"kXSdHODy\":{\"uv\":{\"UeaZ\":[7,37],\"9idl\":[7,37],\"JXoa\":[7,37],\"189q\":[7,37]},\"vertices\":[\"UeaZ\",\"9idl\",\"JXoa\",\"189q\"],\"texture\":0},\"GS2UAsdr\":{\"uv\":{\"hqo1\":[47.99883096104092,58.00656281225761],\"dBZA\":[47.998830961040916,58.962182270070585],\"189q\":[48.77067744619717,58.00656281225761],\"r4ao\":[57,63]},\"vertices\":[\"hqo1\",\"dBZA\",\"189q\",\"r4ao\"],\"texture\":0},\"zPROJMPL\":{\"uv\":{\"U073\":[47.48426663760341,46.8699206692834],\"QLYN\":[50.49814338916592,49.84704282631603],\"hKMU\":[57,43]},\"vertices\":[\"U073\",\"QLYN\",\"hKMU\"],\"texture\":0},\"um95maFA\":{\"uv\":{\"QLYN\":[50.49814338916592,49.84704282631603],\"NKg3\":[48.03558555557217,52.309600659911126],\"JXoa\":[48.77067744619717,56.021814707569234],\"e9Nq\":[47.998830961040916,55.98506011303796]},\"vertices\":[\"QLYN\",\"NKg3\",\"JXoa\",\"e9Nq\"],\"texture\":0},\"gLLoDhLm\":{\"uv\":{\"dL0o\":[44.54389907510341,49.81028823178476],\"U073\":[47.48426663760341,46.8699206692834],\"FG7v\":[38,43]},\"vertices\":[\"dL0o\",\"U073\",\"FG7v\"],\"texture\":0},\"XrXhpH32\":{\"uv\":{\"JXoa\":[48.77067744619717,56.021814707569234],\"189q\":[48.77067744619717,58.00656281225761],\"r4ao\":[57,63]},\"vertices\":[\"JXoa\",\"189q\",\"r4ao\"],\"texture\":0},\"PHNtQeyn\":{\"uv\":{\"yLwd\":[38,63],\"r4ao\":[57,63],\"0WGP\":[47.00645690869716,58.962182270070585],\"dBZA\":[47.998830961040916,58.962182270070585]},\"vertices\":[\"yLwd\",\"r4ao\",\"0WGP\",\"dBZA\"],\"texture\":0},\"liPzEfqW\":{\"uv\":{\"hKMU\":[57,43],\"FG7v\":[38,43],\"U073\":[47.48426663760341,46.8699206692834]},\"vertices\":[\"hKMU\",\"FG7v\",\"U073\"],\"texture\":0},\"v4qpUcrI\":{\"uv\":{\"VVBW\":[27.99999999999997,45.99999999999998],\"F95B\":[27.995276762320934,46.769566382242395],\"e9Nq\":[29.00000480498842,45.99999999999998],\"JXoa\":[28.99520936765631,46.76949719257485]},\"vertices\":[\"VVBW\",\"F95B\",\"e9Nq\",\"JXoa\"],\"texture\":0},\"683lSctq\":{\"uv\":{\"F95B\":[27.000000000000036,44.000200000000014],\"1gaV\":[27.000000000000036,45.98490000000001],\"JXoa\":[28.998800000000035,44.000000000000014],\"189q\":[28.998800000000035,45.984800000000014]},\"vertices\":[\"F95B\",\"1gaV\",\"JXoa\",\"189q\"],\"texture\":0},\"PBAFjQsG\":{\"uv\":{\"1gaV\":[28.093400000000035,45.995599999999975],\"xr2V\":[28.865200000000033,45.995599999999975],\"189q\":[28.000000000000032,43.99999999999997],\"hqo1\":[28.771900000000034,43.99999999999997]},\"vertices\":[\"1gaV\",\"xr2V\",\"189q\",\"hqo1\"],\"texture\":0},\"b5VNOV46\":{\"uv\":{\"xr2V\":[27.000000000000014,46.05009999999995],\"hH9w\":[27.000000000000014,47.00569999999995],\"hqo1\":[28.998700000000014,45.99999999999995],\"dBZA\":[28.998700000000014,46.95559999999995]},\"vertices\":[\"xr2V\",\"hH9w\",\"hqo1\",\"dBZA\"],\"texture\":0},\"jjAO4KBB\":{\"uv\":{\"508A\":[32.998800000000045,51.99089999999998],\"Sfyl\":[32.998800000000045,46.050099999999986],\"0WGP\":[31.00000000000005,52.015799999999984],\"dgnq\":[30.97500000000005,46.07499999999998]},\"vertices\":[\"508A\",\"Sfyl\",\"0WGP\",\"dgnq\"],\"texture\":0},\"JMPal9p9\":{\"uv\":{\"hH9w\":[26.99329999999996,44.9957],\"508A\":[27.98569999999996,44.9957],\"dBZA\":[26.99999999999996,43],\"0WGP\":[27.99229999999996,43]},\"vertices\":[\"hH9w\",\"508A\",\"dBZA\",\"0WGP\"],\"texture\":0},\"f236qOoL\":{\"uv\":{\"Sfyl\":[28.935000669024024,45.99950396359492],\"BxC4\":[26.000000000000043,45.99950396359492],\"dgnq\":[28.964021984606497,44.00000000000004],\"dL0o\":[26.02902131558252,44.00000000000004]},\"vertices\":[\"Sfyl\",\"BxC4\",\"dgnq\",\"dL0o\"],\"texture\":0},\"t7bSlwAR\":{\"uv\":{\"Fol7\":[26.999999999999986,42.00000000000005],\"qYIa\":[26.932007931349446,46.234257766841935],\"U073\":[29.000023624720924,42.00000000000005],\"QLYN\":[28.93203155607038,46.234257766841935]},\"vertices\":[\"Fol7\",\"qYIa\",\"U073\",\"QLYN\"],\"texture\":0},\"EQxYGRcX\":{\"uv\":{\"BxC4\":[27.000000000000007,46.259833997702934],\"Fol7\":[27.00000000000001,42.00157577460038],\"dL0o\":[28.994854621345148,46.25835817601018],\"U073\":[28.99485155275265,42.000000000000014]},\"vertices\":[\"BxC4\",\"Fol7\",\"dL0o\",\"U073\"],\"texture\":0},\"4AzPWzUQ\":{\"uv\":{\"FG7v\":[38,43],\"TJ68\":[32,43],\"yLwd\":[38,63],\"2vAk\":[32,63]},\"vertices\":[\"FG7v\",\"TJ68\",\"yLwd\",\"2vAk\"],\"texture\":0},\"7dh0wunx\":{\"uv\":{\"r4ao\":[57,63],\"hKMU\":[57,43],\"QLYN\":[50.49814338916592,49.84704282631603]},\"vertices\":[\"r4ao\",\"hKMU\",\"QLYN\"],\"texture\":0},\"hRoCiiCT\":{\"uv\":{\"QLYN\":[50.49814338916592,49.84704282631603],\"JXoa\":[48.77067744619717,56.021814707569234],\"r4ao\":[57,63]},\"vertices\":[\"QLYN\",\"JXoa\",\"r4ao\"],\"texture\":0},\"HuridzMw\":{\"uv\":{\"FG7v\":[38,43],\"yLwd\":[38,63],\"dgnq\":[47.00645690869716,52.346355254442386]},\"vertices\":[\"FG7v\",\"yLwd\",\"dgnq\"],\"texture\":0},\"DN5Sv2GO\":{\"uv\":{\"yLwd\":[38,63],\"0WGP\":[47.00645690869716,58.962182270070585],\"dgnq\":[47.00645690869716,52.346355254442386]},\"vertices\":[\"yLwd\",\"0WGP\",\"dgnq\"],\"texture\":0},\"CWiIIR9y\":{\"uv\":{\"M5gB\":[107.50000000000003,44.02149999999999],\"YR8Q\":[108.27180000000003,44.02149999999999],\"1pYf\":[107.50000000000003,41.999999999999986],\"seOF\":[108.27180000000003,42.036799999999985]},\"vertices\":[\"M5gB\",\"YR8Q\",\"1pYf\",\"seOF\"],\"texture\":2},\"1ZkbcrH6\":{\"uv\":{\"Itfe\":[106.96590000000003,32.920099999999984],\"pECK\":[104.02550000000002,35.86049999999999],\"Sfyl\":[106.46310000000003,38.39649999999999]},\"vertices\":[\"Itfe\",\"pECK\",\"Sfyl\"],\"texture\":2},\"Z9AHIHy0\":{\"uv\":{\"Itfe\":[106.96590000000003,32.920099999999984],\"Sfyl\":[106.46310000000003,38.39649999999999],\"19bj\":[107.51720000000003,38.359799999999986]},\"vertices\":[\"Itfe\",\"Sfyl\",\"19bj\"],\"texture\":2},\"K3L7HXHQ\":{\"uv\":{\"U1HT\":[109.97980000000003,35.897199999999984],\"Itfe\":[106.96590000000003,32.920099999999984],\"19bj\":[107.51720000000003,38.359799999999986]},\"vertices\":[\"U1HT\",\"Itfe\",\"19bj\"],\"texture\":2},\"eC3cdf9f\":{\"uv\":{\"19bj\":[26.987556430799227,42.99999999999996],\"VVBW\":[26.99999999999999,46.6709133244011],\"NKg3\":[28.98751227982457,42.99999999999996],\"e9Nq\":[28.999960859246542,46.67081344999155]},\"vertices\":[\"19bj\",\"VVBW\",\"NKg3\",\"e9Nq\"],\"texture\":0},\"nKE6wlGP\":{\"uv\":{\"qYIa\":[24.886191993785594,45.029707436043694],\"19bj\":[27.968733080406103,45.02970743604369],\"QLYN\":[24.884706167427986,43.034853835231736],\"NKg3\":[27.967247254048495,43.03485383523173]},\"vertices\":[\"qYIa\",\"19bj\",\"QLYN\",\"NKg3\"],\"texture\":0},\"Sye201L1\":{\"uv\":{\"Sfyl\":[106.50000000000003,38.336699999999986],\"508A\":[106.50000000000003,44.95249999999999],\"19bj\":[107.55410000000003,38.29999999999999]},\"vertices\":[\"Sfyl\",\"508A\",\"19bj\"],\"texture\":2},\"u0fHHhEp\":{\"uv\":{\"508A\":[106.50000000000003,44.95249999999999],\"hH9w\":[107.51740000000004,44.95249999999999],\"19bj\":[107.55410000000003,38.29999999999999]},\"vertices\":[\"508A\",\"hH9w\",\"19bj\"],\"texture\":2}},\"type\":\"mesh\",\"uuid\":\"3f549e53-85c1-22a8-644c-c74da2eebdf2\"}";

}
