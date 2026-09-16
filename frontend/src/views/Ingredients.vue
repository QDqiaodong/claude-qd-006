<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;align-items:center;gap:12px">
          <span>原料台账</span>
          <el-button size="small" type="success" @click="openCreate">登记原料</el-button>
          <span style="margin-left:auto;color:#909399">
            已过期 {{ ingredients.filter(i => isExpired(i)).length }} 种 / 共 {{ ingredients.length }} 种
          </span>
        </div>
      </template>
      <el-table :data="ingredients" border stripe size="small" v-loading="loading">
        <el-table-column prop="name" label="原料名称" min-width="140" />
        <el-table-column prop="stock" label="当前库存(克)" width="130" sortable />
        <el-table-column prop="expiryDate" label="保质期至" width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="isExpired(row)" type="danger">已过期</el-tag>
            <el-tag v-else-if="isNearExpiry(row)" type="warning">临期</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">盘点修改</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:8px;color:#909399;font-size:12px">
        开备餐批和开火加工都会按菜品配方扣这里库存；缺料或原料过期就开不了批、也推进不了加工。
      </div>
    </el-card>

    <el-dialog v-model="visible" :title="form.id ? '盘点修改' : '登记原料'" width="440px">
      <el-form label-width="100px">
        <el-form-item label="原料名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="当前库存(克)">
          <el-input-number v-model="form.stock" :min="0" :step="1000" style="width:100%" />
        </el-form-item>
        <el-form-item label="保质期至">
          <el-date-picker v-model="form.expiryDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ingredientApi } from '../api'

const ingredients = ref([])
const loading = ref(false)
const visible = ref(false)
const form = reactive({ id: null, name: '', stock: 0, expiryDate: '' })

const today = () => new Date().toISOString().slice(0, 10)
const isExpired = (row) => row.expiryDate && row.expiryDate < today()
const isNearExpiry = (row) => {
  if (!row.expiryDate) return false
  const diff = (new Date(row.expiryDate) - new Date(today())) / 86400000
  return diff >= 0 && diff <= 3
}

const load = async () => {
  loading.value = true
  try {
    ingredients.value = await ingredientApi.list({})
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  Object.assign(form, { id: null, name: '', stock: 0, expiryDate: '' })
  visible.value = true
}

const openEdit = (row) => {
  Object.assign(form, { id: row.id, name: row.name, stock: row.stock, expiryDate: row.expiryDate })
  visible.value = true
}

const submit = async () => {
  try {
    if (form.id) {
      await ingredientApi.update(form.id, { name: form.name, stock: form.stock, expiryDate: form.expiryDate })
    } else {
      await ingredientApi.create({ name: form.name, stock: form.stock, expiryDate: form.expiryDate })
    }
    ElMessage.success('已保存')
    visible.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(load)
</script>
