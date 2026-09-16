<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;align-items:center;gap:12px">
          <span>操作间</span>
          <el-button size="small" type="success" @click="openKitchen()">新增操作间</el-button>
          <span style="margin-left:auto;color:#909399">
            在用 {{ kitchens.filter(k => k.status === '在用').length }} 个 / 共 {{ kitchens.length }} 个
          </span>
        </div>
      </template>
      <el-table :data="kitchens" border stripe size="small" v-loading="loading">
        <el-table-column prop="code" label="编号" width="110" />
        <el-table-column prop="name" label="操作间" min-width="150" />
        <el-table-column prop="kind" label="类型" width="100" />
        <el-table-column label="名下设备" width="110">
          <template #default="{ row }">{{ equipments.filter(e => e.kitchenId === row.id).length }} 台</template>
        </el-table-column>
        <el-table-column label="有问题的设备" width="140">
          <template #default="{ row }">
            <span :style="{ color: badCount(row.id) ? '#e6a23c' : '' }">{{ badCount(row.id) }} 台</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '在用' ? 'success' : row.status === '维修' ? 'warning' : 'info'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="openKitchen(row)">调整</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" style="margin-top:16px">
      <template #header>
        <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap">
          <span>厨具设备</span>
          <el-select v-model="query.kitchenId" placeholder="按操作间" clearable size="small" style="width:170px">
            <el-option v-for="k in kitchens" :key="k.id" :label="k.name" :value="k.id" />
          </el-select>
          <el-select v-model="query.status" placeholder="按状态" clearable size="small" style="width:130px">
            <el-option label="可用" value="可用" />
            <el-option label="停用" value="停用" />
            <el-option label="维修中" value="维修中" />
          </el-select>
          <el-input v-model="query.keyword" placeholder="编号或名称" clearable size="small" style="width:170px" />
          <el-button size="small" type="primary" @click="loadEquipments">查询</el-button>
          <el-button size="small" type="success" @click="openEquipment()">新增设备</el-button>
        </div>
      </template>
      <el-table :data="equipments" border stripe size="small" v-loading="loading">
        <el-table-column prop="code" label="设备编号" width="120" />
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="category" label="类别" width="110" />
        <el-table-column label="归属操作间" width="160">
          <template #default="{ row }">{{ kitchenName(row.kitchenId) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '可用' ? 'success' : row.status === '维修中' ? 'warning' : 'info'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEquipment(row)">调整</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="kitchenVisible" :title="kitchenForm.id ? '调整操作间' : '新增操作间'" width="460px">
      <el-form label-width="90px">
        <el-form-item label="编号">
          <el-input v-model="kitchenForm.code" :disabled="!!kitchenForm.id" placeholder="如 K-06" />
        </el-form-item>
        <el-form-item label="操作间"><el-input v-model="kitchenForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="kitchenForm.kind" style="width:100%">
            <el-option v-for="k in ['粗加工', '热厨', '凉菜', '面点', '洗消']" :key="k" :label="k" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="kitchenForm.status" style="width:100%">
            <el-option label="在用" value="在用" />
            <el-option label="停用" value="停用" />
            <el-option label="维修" value="维修" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="kitchenVisible = false">取消</el-button>
        <el-button type="primary" @click="submitKitchen">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="equipmentVisible" :title="equipForm.id ? '调整设备' : '新增设备'" width="460px">
      <el-form label-width="100px">
        <el-form-item label="设备编号">
          <el-input v-model="equipForm.code" :disabled="!!equipForm.id" placeholder="如 EQ-2007" />
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="equipForm.name" /></el-form-item>
        <el-form-item label="类别">
          <el-select v-model="equipForm.category" style="width:100%">
            <el-option v-for="c in ['灶具', '蒸箱', '冷库', '和面机', '消毒柜']" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="归属操作间">
          <el-select v-model="equipForm.kitchenId" clearable placeholder="可先不归" style="width:100%">
            <el-option v-for="k in kitchens" :key="k.id" :label="k.name" :value="k.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="equipForm.status" style="width:100%">
            <el-option label="可用" value="可用" />
            <el-option label="停用" value="停用" />
            <el-option label="维修中" value="维修中" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="equipmentVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEquipment">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { equipmentApi, kitchenApi } from '../api'

const kitchens = ref([])
const equipments = ref([])
const loading = ref(false)
const query = reactive({ kitchenId: null, status: '', keyword: '' })

const kitchenVisible = ref(false)
const kitchenForm = reactive({ id: null, code: '', name: '', kind: '热厨', status: '在用' })
const equipmentVisible = ref(false)
const equipForm = reactive({ id: null, code: '', name: '', category: '灶具', kitchenId: null, status: '可用' })

const kitchenName = (id) => (id ? kitchens.value.find((k) => k.id === id)?.name || '未归间' : '未归间')
const badCount = (kitchenId) =>
  equipments.value.filter((e) => e.kitchenId === kitchenId && e.status !== '可用').length

const loadKitchens = async () => {
  kitchens.value = await kitchenApi.list({})
}

const loadEquipments = async () => {
  loading.value = true
  try {
    equipments.value = await equipmentApi.list({
      kitchenId: query.kitchenId || undefined,
      status: query.status || undefined,
      keyword: query.keyword || undefined
    })
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

const openKitchen = (row) => {
  if (row) {
    Object.assign(kitchenForm, row)
  } else {
    Object.assign(kitchenForm, { id: null, code: '', name: '', kind: '热厨', status: '在用' })
  }
  kitchenVisible.value = true
}

const submitKitchen = async () => {
  try {
    if (kitchenForm.id) {
      await kitchenApi.update(kitchenForm.id, {
        name: kitchenForm.name,
        kind: kitchenForm.kind,
        status: kitchenForm.status
      })
    } else {
      await kitchenApi.create({ ...kitchenForm })
    }
    ElMessage.success('已保存')
    kitchenVisible.value = false
    await loadKitchens()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const openEquipment = (row) => {
  if (row) {
    Object.assign(equipForm, row)
  } else {
    Object.assign(equipForm, { id: null, code: '', name: '', category: '灶具', kitchenId: null, status: '可用' })
  }
  equipmentVisible.value = true
}

const submitEquipment = async () => {
  try {
    if (equipForm.id) {
      await equipmentApi.update(equipForm.id, {
        name: equipForm.name,
        category: equipForm.category,
        kitchenId: equipForm.kitchenId,
        status: equipForm.status
      })
    } else {
      await equipmentApi.create({ ...equipForm })
    }
    ElMessage.success('已保存')
    equipmentVisible.value = false
    await loadEquipments()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(async () => {
  try {
    await loadKitchens()
  } catch (e) {
    ElMessage.error(e.message)
  }
  await loadEquipments()
})
</script>
